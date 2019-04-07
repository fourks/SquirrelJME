// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
//     Copyright (C) Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package net.multiphasicapps.classfile.register;

import dev.shadowtail.classfile.xlate.JavaStackEnqueueList;
import dev.shadowtail.classfile.xlate.JavaStackResult;
import dev.shadowtail.classfile.xlate.JavaStackShuffleType;
import dev.shadowtail.classfile.xlate.JavaStackState;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.multiphasicapps.classfile.ByteCode;
import net.multiphasicapps.classfile.ClassName;
import net.multiphasicapps.classfile.ConstantValue;
import net.multiphasicapps.classfile.ConstantValueInteger;
import net.multiphasicapps.classfile.ExceptionHandler;
import net.multiphasicapps.classfile.ExceptionHandlerTable;
import net.multiphasicapps.classfile.FieldDescriptor;
import net.multiphasicapps.classfile.FieldReference;
import net.multiphasicapps.classfile.Instruction;
import net.multiphasicapps.classfile.InstructionIndex;
import net.multiphasicapps.classfile.InstructionJumpTarget;
import net.multiphasicapps.classfile.InstructionJumpTargets;
import net.multiphasicapps.classfile.JavaType;
import net.multiphasicapps.classfile.MethodDescriptor;
import net.multiphasicapps.classfile.MethodHandle;
import net.multiphasicapps.classfile.MethodName;
import net.multiphasicapps.classfile.MethodReference;
import net.multiphasicapps.classfile.PrimitiveType;
import net.multiphasicapps.classfile.StackMapTable;
import net.multiphasicapps.classfile.StackMapTableState;

/**
 * This class is used to transform normal byte code into register code that
 * is more optimized for VMs.
 *
 * @since 2019/03/14
 */
public final class ExperimentalTranslator
	implements Translator
{
	/** The input byte code to translate. */
	protected final ByteCode bytecode;
	
	/** Used to build register codes. */
	protected final RegisterCodeBuilder codebuilder =
		new RegisterCodeBuilder();
	
	/** Exception tracker. */
	protected final ExceptionHandlerRanges exceptiontracker;
	
	/** Default field access type, to determine how fields are accessed. */
	protected final FieldAccessTime defaultfieldaccesstime;
	
	/** The instruction throws an exception, it must be checked. */
	private boolean _exceptioncheck;
	
	/** Reverse jump table. */
	private Map<Integer, InstructionJumpTargets> _revjumps;
	
	/** Exception handler combinations to generate. */
	private final List<ExceptionStackAndTable> _usedexceptions =
		new ArrayList<>();
	
	/** Used exception lines. */
	private final List<Integer> _usedexceptionsln =
		new ArrayList<>();
	
	/** Exceptions which were made from some operations. */
	private final List<ExceptionClassStackAndTable> _madeexceptions =
		new ArrayList<>();
	
	/** Made exception lines. */
	private final List<Integer> _madeexceptionsln =
		new ArrayList<>();
	
	/** Object position maps to return label points. */
	private final Map<JavaStackEnqueueList, RegisterCodeLabel> _returns =
		new LinkedHashMap<>();
	
	/** The stacks which have been recorded. */
	private final Map<Integer, JavaStackState> _stacks =
		new LinkedHashMap<>();
	
	/** The current state of the stack. */
	private JavaStackState _stack;
	
	/** The current PC being processed. */
	private int _currentprocesspc =
		-1;
	
	/** Next returning index? */
	private int _nextreturndx;
	
	/** Last registers enqueued. */
	private JavaStackEnqueueList _lastenqueue;
	
	/**
	 * Converts the input byte code to a register based code.
	 *
	 * @param __bc The byte code to translate.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/03/14
	 */
	public ExperimentalTranslator(ByteCode __bc)
		throws NullPointerException
	{
		if (__bc == null)
			throw new NullPointerException("NARG");
		
		this.bytecode = __bc;
		this.exceptiontracker = new ExceptionHandlerRanges(__bc);
		this.defaultfieldaccesstime = ((__bc.isInstanceInitializer() ||
			__bc.isStaticInitializer()) ? FieldAccessTime.INITIALIZER :
			FieldAccessTime.NORMAL);
		
		// Load initial Java stack state from the initial stack map
		JavaStackState s;
		this._stack = (s = JavaStackState.of(__bc.stackMapTable().get(0),
			__bc.writtenLocals()));
		this._stacks.put(0, s);
		
		// Load reverse jump table
		this._revjumps = __bc.reverseJumpTargets();
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2019/03/14
	 */
	@Override
	public RegisterCode convert()
	{
		ByteCode bytecode = this.bytecode;
		RegisterCodeBuilder codebuilder = this.codebuilder;
		Map<Integer, InstructionJumpTargets> revjumps = this._revjumps;
		
		// Stacks for target methods
		Map<Integer, JavaStackState> stacks = this._stacks;
		
		// Process every instruction
		for (Instruction inst : bytecode)
		{
			// Debug
			todo.DEBUG.note("Xlate %s", inst);
			
			// Current processing this address
			int addr = inst.address();
			this._currentprocesspc = addr;
			
			// Clear the exception check since not every instruction will
			// generate an exception, this will reduce the code size greatly
			this._exceptioncheck = false;
			
			// Set source line for this instruction for debugging purposes
			codebuilder.setSourceLine(bytecode.lineOfAddress(addr));
			
			// {@squirreljme.error JC2s No recorded stack state for this
			// position. (The address to check)}
			JavaStackState stack = stacks.get(addr);
			if (stack == null)
				throw new IllegalArgumentException("JC2s " + addr);
			
			// If this instruction is jumped to from a future address then
			// we need to invalidate our cached stack items
			// This is done to keep the code generator simpler so it does not
			// have to mess around with various different steps and we can
			// keep processing the method linearly
			InstructionJumpTargets ijt = revjumps.get(addr);
			if (ijt != null && ijt.hasLaterAddress(addr))
				throw new todo.TODO();
			
			// Add label to refer to this instruction in Java terms
			codebuilder.label("java", addr);
			
			// Process instructions
			this.__process(inst);
			
			// If an exception is thrown it needs to be handled accordingly
			// This means uncounting anything on the stack, reading the
			// exception register value, then jumping to the exception handler
			// for this instruction
			if (this._exceptioncheck)
			{
				// Create jumping label for this exception
				RegisterCodeLabel ehlab = this.__exceptionTrack(addr);
				
				// Just create a jump here
				codebuilder.add(
					RegisterOperationType.JUMP_IF_EXCEPTION, ehlab);
			}
			
			// New stack state would have been replaced
			JavaStackState newstack = this._stack;
			
			// Set target stack states for destinations of this instruction
			// Calculate the exception state only if it is needed
			JavaStackState hypoex = null;
			ijt = inst.jumpTargets();
			if (ijt != null && !ijt.isEmpty())
				for (int i = 0, n = ijt.size(); i < n; i++)
				{
					int jta = ijt.get(i).target();
					
					// Lazily calculate the exception handler since it might
					// not always be needed
					boolean isexception = ijt.isException(i);
					if (isexception && hypoex == null)
						hypoex = newstack.doExceptionHandler(new JavaType(
							new ClassName("java/lang/Throwable"))).after();
					
					// The type of stack to target
					JavaStackState use = (isexception ? hypoex : newstack);
					
					// Is empty state
					JavaStackState dss = stacks.get(jta);
					if (dss == null)
						stacks.put(jta, use);
					
					// May need adapting
					else if (!use.equals(dss))
					{
						// Debug
						todo.DEBUG.note("Adapt %s -> %s", use, dss);
						
						throw new todo.TODO();
					}
				}
		}
		
		// Invalidate source lines for the exception tables
		codebuilder.setSourceLine(-1);
		
		// Generates all of the made exceptions which initialize an exception
		// then handle it
		List<ExceptionClassStackAndTable> madeexceptions = this._madeexceptions;
		if (!madeexceptions.isEmpty())
			for (int i = 0, n = madeexceptions.size(); i < n; i++)
				this.__madeExceptionGenerate(madeexceptions.get(i), i);
		
		// If we need to generate exception tables, do it now
		List<ExceptionStackAndTable> usedexceptions = this._usedexceptions;
		if (!usedexceptions.isEmpty())
			for (int i = 0, n = usedexceptions.size(); i < n; i++)
				this.__exceptionGenerate(usedexceptions.get(i), i);
		
		// Build the final code table
		return codebuilder.build();
	}
	
	/**
	 * Generates exception handler code for the given index.
	 *
	 * @param __ec The combo to generate for.
	 * @param __edx The index of the used exception table.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/03/22
	 */
	private final void __exceptionGenerate(ExceptionStackAndTable __ec, int __edx)
		throws NullPointerException
	{
		if (__ec == null)
			throw new NullPointerException("NARG");
		
		RegisterCodeBuilder codebuilder = this.codebuilder;
		JavaStackState ehstack = __ec.stack;
		ExceptionHandlerTable ehtable = __ec.table;
		JavaStackEnqueueList allenq = ehstack.possibleEnqueue();
		
		// Set line for code generation
		codebuilder.setSourceLine(this._usedexceptionsln.get(__edx));
		
		// Debug
		todo.DEBUG.note("Exception gen %s,%s -> %d", ehstack, ehtable, __edx);
		
		// If the exception handler table is empty, we are just going to
		// go up the stack anyway, so there is no point in generating our
		// own handler!
		if (ehtable.isEmpty())
		{
			// If there was already a return point which represents how we
			// would uncount and return, then just make the exception point
			// at this jump point. So when the labels are resolved no jumps
			// are generated, the JUMP_ON_EXCEPTION will just point to one
			// of the cleanup and return points
			Map<JavaStackEnqueueList, RegisterCodeLabel> rs = this._returns;
			RegisterCodeLabel rcl = rs.get(allenq);
			if (rcl != null)
			{
				// Just point this exception to that return location
				codebuilder.label("exception", __edx,
					codebuilder.labelTarget(rcl));
				return;
			}
			
			// Label here has usual and just create a return
			codebuilder.label("exception", __edx);
			this.__return(allenq);
			
			// Do no more work
			return;
		}
		
		// Mark the current position as the handler, so other parts of the
		// code can jump here
		codebuilder.label("exception", __edx);
		
		// Just setup a target stack
		JavaStackResult result = ehstack.doExceptionHandler(
			new JavaType(new ClassName("java/lang/Throwable")));
		
		// Even though everything is done processing, this is done for
		// the __javaLabel() call which refers to the current stack
		JavaStackState poststack;
		this._stack = (poststack = result.after());
		
		// Un-count all stack entries
		JavaStackEnqueueList seq = result.enqueue();
		for (int i = 0, n = seq.size(); i < n; i++)
			codebuilder.add(RegisterOperationType.UNCOUNT, seq.get(i));
		
		// For each exception type, perform a check and a jump to the target
		int sbreg = result.out(0).register;
		for (ExceptionHandler eh : ehtable)
			codebuilder.add(
				RegisterOperationType.JUMP_IF_INSTANCE_GET_EXCEPTION,
				eh.type(), this.__javaLabel(eh.handlerAddress()),
				sbreg);
		
		// For returning destroy everything then go to the return route
		// There would have been no pushed stack entry so just ignore it
		JavaStackResult boom = poststack.doDestroy(false);
		this.__return(boom.enqueue().onlyLocals());
	}
	
	/**
	 * Creates and stores an exception combo.
	 *
	 * @param __pc The address to get.
	 * @return The exception combo.
	 * @since 2019/04/02
	 */
	private final ExceptionStackAndTable __exceptionCombo(int __pc)
	{
		// Create combo for the stack and exception data
		ExceptionStackAndTable ec = this.exceptiontracker.stackAndTable(
			this._stack, __pc);
		
		// Add the combo to the list
		List<ExceptionStackAndTable> usedexceptions = this._usedexceptions;
		if (usedexceptions.indexOf(ec) < 0)
		{
			usedexceptions.add(ec);
			this._usedexceptionsln.add(__pc);
		}
		
		return ec;
	}
	
	/**
	 * Handles the process of exceptions, this just defers the generation
	 * of exception data until the end.
	 *
	 * @param __pc The current PC address.
	 * @return The exception combo index.
	 * @since 2019/03/22
	 */
	private final RegisterCodeLabel __exceptionTrack(int __pc)
	{
		// Create combo and search for it
		return new RegisterCodeLabel("exception",
			this._usedexceptions.indexOf(this.__exceptionCombo(__pc)));
	}
	
	/**
	 * Generates an access to a field.
	 *
	 * @param __at The type of access to perform.
	 * @param __fr The reference to the field.
	 * @return The accessed field.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/03/24
	 */
	private final AccessedField __fieldAccess(FieldAccessType __at,
		FieldReference __fr)
		throws NullPointerException
	{
		if (__at == null || __fr == null)
			throw new NullPointerException("NARG");
		
		// Accessing final fields of another class will always be treated as
		// normal despite being in the constructor of a class
		ByteCode bytecode = this.bytecode;
		if (!bytecode.thisType().equals(__fr.className()))
			return new AccessedField(FieldAccessTime.NORMAL, __at, __fr);
		return new AccessedField(this.defaultfieldaccesstime, __at, __fr);
	}
	
	/**
	 * Creates a label referring to a Java address, this could be replaced
	 * potentially.
	 *
	 * This method should be called after all stack operations have been
	 * performed, otherwise it may result in a confusing state.
	 *
	 * @param __addr The address to point to.
	 * @return The label to jump to.
	 * @since 2019/03/27
	 */
	private final RegisterCodeLabel __javaLabel(int __addr)
	{
		// If the address we are jumping to is at a future point we can use
		// a simple jump ahead
		int currentprocesspc = this._currentprocesspc;
		if (__addr >= currentprocesspc)
			return new RegisterCodeLabel("java", __addr);
		
		// Otherwise we will need to uncount any local variables which are
		// not at the previous location. Additionally since that past
		// instruction would have had its caches and counts completely wiped
		// we need to do the same when entering to make sure it matches the
		// same state
		throw new todo.TODO();
	}
	
	/**
	 * Generates made exceptions.
	 *
	 * @param __me The exception to make.
	 * @param __d The index of this exception.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/04/02
	 */
	private final void __madeExceptionGenerate(ExceptionClassStackAndTable __me, int __d)
		throws NullPointerException
	{
		if (__me == null)
			throw new NullPointerException("NARG");
		
		// Used in generating the make area
		RegisterCodeBuilder codebuilder = this.codebuilder;
		
		// Set line for code generation
		codebuilder.setSourceLine(this._madeexceptionsln.get(__d));
		
		// Label for here
		codebuilder.label("makeexception", __d);
		
		// Allocate exception
		int tempreg = this._stack.usedregisters;
		codebuilder.add(RegisterOperationType.NEW,
			__me.name, tempreg);
		
		// Call the constructor on it
		codebuilder.add(RegisterOperationType.INVOKE_METHOD,
			new InvokedMethod(InvokeType.SPECIAL, new MethodHandle(__me.name,
			new MethodName("<init>"), new MethodDescriptor("()V"))),
			new RegisterList(tempreg));
		
		// Load it into exception register
		codebuilder.add(RegisterOperationType.SET_AND_FLAG_EXCEPTION,
			tempreg);
		
		// Generate jump to exception
		codebuilder.add(RegisterOperationType.JUMP,
			new RegisterCodeLabel("exception",
			this._usedexceptions.indexOf(__me.stackandtable)));
	}
	
	/**
	 * Defers for later an exception generator.
	 *
	 * @param __cn The class name to generate.
	 * @return The label to the generation point.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/04/02
	 */
	private final RegisterCodeLabel __makeException(ClassName __cn)
		throws NullPointerException
	{
		if (__cn == null)
			throw new NullPointerException("NARG");
		
		// Track exception, the stack state, and the exception handle
		ExceptionClassStackAndTable me = new ExceptionClassStackAndTable(__cn,
			this.__exceptionCombo(this._currentprocesspc));
		
		// Find index, add if missing
		List<ExceptionClassStackAndTable> madeexceptions = this._madeexceptions;
		int dx = madeexceptions.indexOf(me);
		if (dx < 0)
		{
			madeexceptions.add((dx = madeexceptions.size()), me);
			this._madeexceptionsln.add(this._currentprocesspc);
		}
		
		return new RegisterCodeLabel("makeexception", dx);
	}
	
	/**
	 * Processes a single instruction.
	 *
	 * @param __i The instruction to process.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/03/14
	 */
	private final void __process(Instruction __i)
		throws NullPointerException
	{
		if (__i == null)
			throw new NullPointerException("NARG");
		
		// Depends on the operation to process
		int op;
		switch ((op = __i.operation()))
		{
			case InstructionIndex.AALOAD:
				this.__runArrayLoad(null);
				break;
			
			case InstructionIndex.AASTORE:
				this.__runArrayStore(null);
				break;
			
			case InstructionIndex.ALOAD:
			case InstructionIndex.WIDE_ALOAD:
				this.__runLoad(__i.intArgument(0));
				break;
			
			case InstructionIndex.ALOAD_0:
			case InstructionIndex.ALOAD_1:
			case InstructionIndex.ALOAD_2:
			case InstructionIndex.ALOAD_3:
				this.__runLoad(op - InstructionIndex.ALOAD_0);
				break;
			
			case InstructionIndex.ARRAYLENGTH:
				this.__runArrayLength();
				break;
			
			case InstructionIndex.ASTORE:
			case InstructionIndex.WIDE_ASTORE:
				this.__runStore(__i.intArgument(0));
				break;
			
			case InstructionIndex.ASTORE_0:
			case InstructionIndex.ASTORE_1:
			case InstructionIndex.ASTORE_2:
			case InstructionIndex.ASTORE_3:
				this.__runStore(op - InstructionIndex.ASTORE_0);
				break;
			
			case InstructionIndex.ATHROW:
				this.__runAThrow();
				break;
				
			case InstructionIndex.BALOAD:
				this.__runArrayLoad(PrimitiveType.BYTE);
				break;
			
			case InstructionIndex.BASTORE:
				this.__runArrayStore(PrimitiveType.BYTE);
				break;
				
			case InstructionIndex.BIPUSH:
				this.__runLdc(new ConstantValueInteger(__i.intArgument(0)));
				break;
			
			case InstructionIndex.CALOAD:
				this.__runArrayLoad(PrimitiveType.CHARACTER);
				break;
			
			case InstructionIndex.CASTORE:
				this.__runArrayStore(PrimitiveType.CHARACTER);
				break;
				
			case InstructionIndex.DALOAD:
				this.__runArrayLoad(PrimitiveType.DOUBLE);
				break;
			
			case InstructionIndex.DASTORE:
				this.__runArrayStore(PrimitiveType.DOUBLE);
				break;
				
			case InstructionIndex.DLOAD:
			case InstructionIndex.WIDE_DLOAD:
				this.__runLoad(__i.intArgument(0));
				break;
			
			case InstructionIndex.DLOAD_0:
			case InstructionIndex.DLOAD_1:
			case InstructionIndex.DLOAD_2:
			case InstructionIndex.DLOAD_3:
				this.__runLoad(op - InstructionIndex.DLOAD_0);
				break;
			
			case InstructionIndex.DSTORE:
			case InstructionIndex.WIDE_DSTORE:
				this.__runStore(__i.intArgument(0));
				break;
			
			case InstructionIndex.DSTORE_0:
			case InstructionIndex.DSTORE_1:
			case InstructionIndex.DSTORE_2:
			case InstructionIndex.DSTORE_3:
				this.__runStore(op - InstructionIndex.DSTORE_0);
				break;
			
			case InstructionIndex.DUP:
				this.__runShuffle(JavaStackShuffleType.DUP);
				break;
			
			case InstructionIndex.DUP_X1:
				this.__runShuffle(JavaStackShuffleType.DUP_X1);
				break;
			
			case InstructionIndex.DUP_X2:
				this.__runShuffle(JavaStackShuffleType.DUP_X2);
				break;
			
			case InstructionIndex.DUP2:
				this.__runShuffle(JavaStackShuffleType.DUP2);
				break;
			
			case InstructionIndex.DUP2_X1:
				this.__runShuffle(JavaStackShuffleType.DUP2_X1);
				break;
			
			case InstructionIndex.DUP2_X2:
				this.__runShuffle(JavaStackShuffleType.DUP2_X2);
				break;
				
			case InstructionIndex.FALOAD:
				this.__runArrayLoad(PrimitiveType.FLOAT);
				break;
			
			case InstructionIndex.FASTORE:
				this.__runArrayStore(PrimitiveType.FLOAT);
				break;
				
			case InstructionIndex.FLOAD:
			case InstructionIndex.WIDE_FLOAD:
				this.__runLoad(__i.intArgument(0));
				break;
			
			case InstructionIndex.FLOAD_0:
			case InstructionIndex.FLOAD_1:
			case InstructionIndex.FLOAD_2:
			case InstructionIndex.FLOAD_3:
				this.__runLoad(op - InstructionIndex.FLOAD_0);
				break;
			
			case InstructionIndex.FSTORE:
			case InstructionIndex.WIDE_FSTORE:
				this.__runStore(__i.intArgument(0));
				break;
			
			case InstructionIndex.FSTORE_0:
			case InstructionIndex.FSTORE_1:
			case InstructionIndex.FSTORE_2:
			case InstructionIndex.FSTORE_3:
				this.__runStore(op - InstructionIndex.FSTORE_0);
				break;
			
			case InstructionIndex.GETFIELD:
				this.__runGetField(__i.<FieldReference>argument(0,
					FieldReference.class));
				break;
			
			case InstructionIndex.GOTO:
			case InstructionIndex.GOTO_W:
				this.__runGoto(__i.<InstructionJumpTarget>argument(0,
					InstructionJumpTarget.class));
				break;
				
			case InstructionIndex.IALOAD:
				this.__runArrayLoad(PrimitiveType.INTEGER);
				break;
			
			case InstructionIndex.IASTORE:
				this.__runArrayStore(PrimitiveType.INTEGER);
				break;
			
			case InstructionIndex.ICONST_M1:
			case InstructionIndex.ICONST_0:
			case InstructionIndex.ICONST_1:
			case InstructionIndex.ICONST_2:
			case InstructionIndex.ICONST_3:
			case InstructionIndex.ICONST_4:
			case InstructionIndex.ICONST_5:
				this.__runLdc(new ConstantValueInteger(
					op - InstructionIndex.ICONST_0));
				break;
			
			case InstructionIndex.IFEQ:
			case InstructionIndex.IFNULL:
				this.__runIfZero(false, CompareType.EQUALS,
					__i.<InstructionJumpTarget>argument(0,
						InstructionJumpTarget.class));
				break;
				
			case InstructionIndex.IFNE:
			case InstructionIndex.IFNONNULL:
				this.__runIfZero(false, CompareType.NOT_EQUALS,
					__i.<InstructionJumpTarget>argument(0,
						InstructionJumpTarget.class));
				break;
				
			case InstructionIndex.IFLT:
				this.__runIfZero(false, CompareType.LESS_THAN,
					__i.<InstructionJumpTarget>argument(0,
						InstructionJumpTarget.class));
				break;
				
			case InstructionIndex.IFGE:
				this.__runIfZero(false, CompareType.GREATER_THAN_OR_EQUALS,
					__i.<InstructionJumpTarget>argument(0,
						InstructionJumpTarget.class));
				break;
				
			case InstructionIndex.IFGT:
				this.__runIfZero(false, CompareType.GREATER_THAN,
					__i.<InstructionJumpTarget>argument(0,
						InstructionJumpTarget.class));
				break;
				
			case InstructionIndex.IFLE:
				this.__runIfZero(false, CompareType.LESS_THAN_OR_EQUALS,
					__i.<InstructionJumpTarget>argument(0,
						InstructionJumpTarget.class));
				break;
			
			case InstructionIndex.ILOAD:
			case InstructionIndex.WIDE_ILOAD:
				this.__runLoad(__i.<Integer>argument(0, Integer.class));
				break;
			
			case InstructionIndex.ILOAD_0:
			case InstructionIndex.ILOAD_1:
			case InstructionIndex.ILOAD_2:
			case InstructionIndex.ILOAD_3:
				this.__runLoad(op - InstructionIndex.ILOAD_0);
				break;
			
			case InstructionIndex.INVOKEINTERFACE:
				this.__runInvoke(InvokeType.INTERFACE,
					__i.<MethodReference>argument(0, MethodReference.class));
				break;
			
			case InstructionIndex.INVOKESPECIAL:
				this.__runInvoke(InvokeType.SPECIAL,
					__i.<MethodReference>argument(0, MethodReference.class));
				break;
			
			case InstructionIndex.INVOKESTATIC:
				this.__runInvoke(InvokeType.STATIC,
					__i.<MethodReference>argument(0, MethodReference.class));
				break;
				
			case InstructionIndex.INVOKEVIRTUAL:
				this.__runInvoke(InvokeType.VIRTUAL,
					__i.<MethodReference>argument(0, MethodReference.class));
				break;
			
			case InstructionIndex.ISTORE:
			case InstructionIndex.WIDE_ISTORE:
				this.__runStore(__i.intArgument(0));
				break;
			
			case InstructionIndex.ISTORE_0:
			case InstructionIndex.ISTORE_1:
			case InstructionIndex.ISTORE_2:
			case InstructionIndex.ISTORE_3:
				this.__runStore(op - InstructionIndex.ISTORE_0);
				break;
				
			case InstructionIndex.LALOAD:
				this.__runArrayLoad(PrimitiveType.LONG);
				break;
			
			case InstructionIndex.LASTORE:
				this.__runArrayStore(PrimitiveType.LONG);
				break;
			
			case InstructionIndex.LDC:
			case InstructionIndex.LDC_W:
			case InstructionIndex.LDC2_W:
				this.__runLdc(__i.<ConstantValue>argument(
					0, ConstantValue.class));
				break;
				
			case InstructionIndex.LLOAD:
			case InstructionIndex.WIDE_LLOAD:
				this.__runLoad(__i.intArgument(0));
				break;
			
			case InstructionIndex.LLOAD_0:
			case InstructionIndex.LLOAD_1:
			case InstructionIndex.LLOAD_2:
			case InstructionIndex.LLOAD_3:
				this.__runLoad(op - InstructionIndex.LLOAD_0);
				break;
			
			case InstructionIndex.LSTORE:
			case InstructionIndex.WIDE_LSTORE:
				this.__runStore(__i.intArgument(0));
				break;
			
			case InstructionIndex.LSTORE_0:
			case InstructionIndex.LSTORE_1:
			case InstructionIndex.LSTORE_2:
			case InstructionIndex.LSTORE_3:
				this.__runStore(op - InstructionIndex.LSTORE_0);
				break;
			
			case InstructionIndex.NEW:
				this.__runNew(__i.<ClassName>argument(0, ClassName.class));
				break;
			
			case InstructionIndex.NEWARRAY:
				this.__runNewArray(__i.<PrimitiveType>argument(0,
					PrimitiveType.class).toClassName());
				break;
			
			case InstructionIndex.NOP:
				break;
			
			case InstructionIndex.POP:
				this.__runShuffle(JavaStackShuffleType.POP);
				break;
			
			case InstructionIndex.POP2:
				this.__runShuffle(JavaStackShuffleType.POP2);
				break;
			
			case InstructionIndex.PUTFIELD:
				this.__runPutField(__i.<FieldReference>argument(0,
					FieldReference.class));
				break;
			
			case InstructionIndex.RETURN:
				this.__runReturn(false);
				break;
			
			case InstructionIndex.ARETURN:
			case InstructionIndex.DRETURN:
			case InstructionIndex.FRETURN:
			case InstructionIndex.IRETURN:
			case InstructionIndex.LRETURN:
				this.__runReturn(true);
				break;
				
			case InstructionIndex.SALOAD:
				this.__runArrayLoad(PrimitiveType.SHORT);
				break;
			
			case InstructionIndex.SASTORE:
				this.__runArrayStore(PrimitiveType.SHORT);
				break;
				
			case InstructionIndex.SIPUSH:
				this.__runLdc(new ConstantValueInteger(__i.intArgument(0)));
				break;
			
			case InstructionIndex.SWAP:
				this.__runShuffle(JavaStackShuffleType.SWAP);
				break;
				
			case InstructionIndex.IADD:
				this.__runMathABC(RegisterOperationType.INT_ADD,
					JavaType.INTEGER);
				break;

			case InstructionIndex.ISUB:
				this.__runMathABC(RegisterOperationType.INT_SUB,
					JavaType.INTEGER);
				break;

			case InstructionIndex.IMUL:
				this.__runMathABC(RegisterOperationType.INT_MUL,
					JavaType.INTEGER);
				break;

			case InstructionIndex.IDIV:
				this.__runMathABC(RegisterOperationType.INT_DIV,
					JavaType.INTEGER);
				break;

			case InstructionIndex.IREM:
				this.__runMathABC(RegisterOperationType.INT_REM,
					JavaType.INTEGER);
				break;

			case InstructionIndex.ISHL:
				this.__runMathABC(RegisterOperationType.INT_SHL,
					JavaType.INTEGER);
				break;

			case InstructionIndex.ISHR:
				this.__runMathABC(RegisterOperationType.INT_SHR,
					JavaType.INTEGER);
				break;

			case InstructionIndex.IUSHR:
				this.__runMathABC(RegisterOperationType.INT_USHR,
					JavaType.INTEGER);
				break;

			case InstructionIndex.IAND:
				this.__runMathABC(RegisterOperationType.INT_AND,
					JavaType.INTEGER);
				break;

			case InstructionIndex.IOR:
				this.__runMathABC(RegisterOperationType.INT_OR,
					JavaType.INTEGER);
				break;

			case InstructionIndex.IXOR:
				this.__runMathABC(RegisterOperationType.INT_XOR,
					JavaType.INTEGER);
				break;

			case InstructionIndex.LADD:
				this.__runMathABC(RegisterOperationType.LONG_ADD,
					JavaType.LONG);
				break;

			case InstructionIndex.LSUB:
				this.__runMathABC(RegisterOperationType.LONG_SUB,
					JavaType.LONG);
				break;

			case InstructionIndex.LMUL:
				this.__runMathABC(RegisterOperationType.LONG_MUL,
					JavaType.LONG);
				break;

			case InstructionIndex.LDIV:
				this.__runMathABC(RegisterOperationType.LONG_DIV,
					JavaType.LONG);
				break;

			case InstructionIndex.LREM:
				this.__runMathABC(RegisterOperationType.LONG_REM,
					JavaType.LONG);
				break;

			case InstructionIndex.LSHL:
				this.__runMathABC(RegisterOperationType.LONG_SHL,
					JavaType.LONG);
				break;

			case InstructionIndex.LSHR:
				this.__runMathABC(RegisterOperationType.LONG_SHR,
					JavaType.LONG);
				break;

			case InstructionIndex.LUSHR:
				this.__runMathABC(RegisterOperationType.LONG_USHR,
					JavaType.LONG);
				break;

			case InstructionIndex.LAND:
				this.__runMathABC(RegisterOperationType.LONG_AND,
					JavaType.LONG);
				break;

			case InstructionIndex.LOR:
				this.__runMathABC(RegisterOperationType.LONG_OR,
					JavaType.LONG);
				break;

			case InstructionIndex.LXOR:
				this.__runMathABC(RegisterOperationType.LONG_XOR,
					JavaType.LONG);
				break;

			case InstructionIndex.FADD:
				this.__runMathABC(RegisterOperationType.FLOAT_ADD,
					JavaType.FLOAT);
				break;

			case InstructionIndex.FSUB:
				this.__runMathABC(RegisterOperationType.FLOAT_SUB,
					JavaType.FLOAT);
				break;

			case InstructionIndex.FMUL:
				this.__runMathABC(RegisterOperationType.FLOAT_MUL,
					JavaType.FLOAT);
				break;

			case InstructionIndex.FDIV:
				this.__runMathABC(RegisterOperationType.FLOAT_DIV,
					JavaType.FLOAT);
				break;

			case InstructionIndex.FREM:
				this.__runMathABC(RegisterOperationType.FLOAT_REM,
					JavaType.FLOAT);
				break;

			case InstructionIndex.DADD:
				this.__runMathABC(RegisterOperationType.DOUBLE_ADD,
					JavaType.DOUBLE);
				break;

			case InstructionIndex.DSUB:
				this.__runMathABC(RegisterOperationType.DOUBLE_SUB,
					JavaType.DOUBLE);
				break;

			case InstructionIndex.DMUL:
				this.__runMathABC(RegisterOperationType.DOUBLE_MUL,
					JavaType.DOUBLE);
				break;

			case InstructionIndex.DDIV:
				this.__runMathABC(RegisterOperationType.DOUBLE_DIV,
					JavaType.DOUBLE);
				break;

			case InstructionIndex.DREM:
				this.__runMathABC(RegisterOperationType.DOUBLE_REM,
					JavaType.DOUBLE);
				break;

			default:
				throw new todo.TODO(__i.toString());
		}
	}
	
	/**
	 * If anything has been previous enqueued then generate code to clear it.
	 *
	 * @since 2019/03/30
	 */
	private final void __refClear()
	{
		// Do nothing if nothing has been enqueued
		JavaStackEnqueueList lastenqueue = this._lastenqueue;
		if (lastenqueue == null)
			return;
		
		// Generate instruction to clear the enqueue
		this.codebuilder.add(RegisterOperationType.REF_CLEAR);
		
		// No need to clear anymore
		this._lastenqueue = null;
	}
	
	/**
	 * Generates code to enqueue registers, if there are any.
	 *
	 * @param __r The registers to enqueue.
	 * @return True if the enqueue list was not empty.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/03/30
	 */
	private final boolean __refEnqueue(JavaStackEnqueueList __r)
		throws NullPointerException
	{
		if (__r == null)
			throw new NullPointerException("NARG");
		
		// Nothing to enqueue?
		if (__r.isEmpty())
		{
			this._lastenqueue = null;
			return false;
		}
		
		// Generate enqueue and set for clearing next time
		this.codebuilder.add(RegisterOperationType.REF_ENQUEUE,
			new RegisterList(__r.registers()));
		this._lastenqueue = __r;
		
		// Did enqueue something
		return true;
	}
	
	/**
	 * Generate code or a jump for a return using the given object position
	 * snapshot.
	 *
	 * @param __ops The position snapshot to use.
	 * @return The label of the target that is used.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/03/22
	 */
	private final RegisterCodeLabel __return(JavaStackEnqueueList __ops)
		throws NullPointerException
	{
		if (__ops == null)
			throw new NullPointerException("NARG");
		
		RegisterCodeBuilder codebuilder = this.codebuilder;
		
		// Try to get existing labels
		Map<JavaStackEnqueueList, RegisterCodeLabel> returns = this._returns;
		RegisterCodeLabel label = returns.get(__ops);
		
		// Debug
		todo.DEBUG.note("return %s -> %s", __ops, label);
		
		// If the object operations is empty and nothing needs to be uncounted
		// we can just return directly. This is faster than jumping to another
		// point that returns
		if (__ops.isEmpty())
		{
			// Always create the label if it does not exist
			// This might be used by exception handlers potentially
			if (label == null)
			{
				label = codebuilder.label("return", this._nextreturndx++);
				returns.put(__ops, label);
			}
			
			// Generate return
			codebuilder.add(RegisterOperationType.RETURN);
		}
		
		// If the return for this operation has already been handled, just
		// do not bother duplicating it. As an optimization to group as many
		// returns up as possible, only a single entry will be uncounted and
		// this method will recursively handle returns until the state is
		// empty
		else if (label == null)
		{
			// Create label at this point and store it for this cleanup state
			label = codebuilder.label("return", this._nextreturndx++);
			returns.put(__ops, label);
			
			// Uncount the top-most entry
			int top = __ops.size() - 1;
			codebuilder.add(RegisterOperationType.UNCOUNT, __ops.get(top));
			
			// Recursively handle this return point, this will generate
			// other cleanup points that can be jumped to. So if this one
			// cleans up 0, 1, and 3 then a cleanup that just does 0 will
			// jump to a point near this.
			this.__return(__ops.trimTop());
			
			// Return the original label for this point!
			return label;
		}
		
		// Otherwise we just perform a jump to that point since it shares
		// the same cleanup code
		else
			codebuilder.add(RegisterOperationType.JUMP, label);
		
		return label;
	}
	
	/**
	 * Get length of array.
	 *
	 * Array operations have a non-standard exception
	 *
	 * @since 2019/04/02
	 */
	private final void __runArrayLength()
	{
		// [array] -> [len]
		JavaStackResult result = this._stack.doStack(1, JavaType.INTEGER);
		this._stack = result.after();
		
		// Possibly clear the instance later
		this.__refEnqueue(result.enqueue());
		
		// Cannot be null
		RegisterCodeBuilder codebuilder = this.codebuilder;
		codebuilder.add(RegisterOperationType.IFNULL_REF_CLEAR,
			result.in(0).register, this.__makeException(
				new ClassName("java/lang/NullPointerException")));
		
		// Get length
		codebuilder.add(RegisterOperationType.ARRAY_LENGTH,
			result.in(0).register,
			result.out(0).register);
		
		// Clear references
		this.__refClear();
	}
	
	/**
	 * Load fromarray.
	 *
	 * @param __pt The type to load, {@code null} is object.
	 * @since 2019/04/02
	 */
	private final void __runArrayLoad(PrimitiveType __pt)
	{
		// [array, index] -> [value]
		JavaStackResult result = this._stack.doStack(2, (__pt == null ?
			new JavaType(new ClassName("java/lang/Object")) :
			__pt.stackJavaType()));
		this._stack = result.after();
		
		// Possibly clear the instance later
		this.__refEnqueue(result.enqueue());
		
		// Check for NPE, and OOB
		RegisterCodeBuilder codebuilder = this.codebuilder;
		codebuilder.add(RegisterOperationType.IFNULL_REF_CLEAR,
			result.in(0).register, this.__makeException(
				new ClassName("java/lang/NullPointerException")));
		codebuilder.add(RegisterOperationType.ARRAY_BOUND_CHECK_AND_REF_CLEAR,
			result.in(0).register, result.in(1).register, this.__makeException(
				new ClassName("java/lang/IndexOutOfBoundsException")));
		
		// Generate
		codebuilder.add(DataType.of(__pt).arrayOperation(false),
			result.in(0).register,
			result.in(1).register,
			result.out(0).register);
		
		// Sign-extend signed types?
		if (__pt == PrimitiveType.BYTE || __pt == PrimitiveType.SHORT)
			codebuilder.add((__pt == PrimitiveType.BYTE ?
					RegisterOperationType.SIGN_X8 :
					RegisterOperationType.SIGN_X16),
				result.out(0).register);
		
		// Clear references
		this.__refClear();
	}
	
	/**
	 * Store into array.
	 *
	 * @param __pt The type to store, {@code null} is object.
	 * @since 2019/04/02
	 */
	private final void __runArrayStore(PrimitiveType __pt)
	{
		// [array, index, value]
		JavaStackResult result = this._stack.doStack(3);
		this._stack = result.after();
		
		// Possibly clear the instance or value later
		this.__refEnqueue(result.enqueue());
		
		// Check for NPE and OOB
		RegisterCodeBuilder codebuilder = this.codebuilder;
		codebuilder.add(RegisterOperationType.IFNULL_REF_CLEAR,
			result.in(0).register, this.__makeException(
				new ClassName("java/lang/NullPointerException")));
		codebuilder.add(RegisterOperationType.ARRAY_BOUND_CHECK_AND_REF_CLEAR,
			result.in(0).register, result.in(1).register, this.__makeException(
				new ClassName("java/lang/IndexOutOfBoundsException")));
		
		// Check for store exception
		if (__pt == null)
			codebuilder.add(
				RegisterOperationType.ARRAY_STORE_CHECK_AND_REF_CLEAR,
				result.in(0).register, result.in(2).register,
					this.__makeException(
					new ClassName("java/lang/ArrayStoreException")));
		
		// Generate
		this.codebuilder.add(DataType.of(__pt).arrayOperation(true),
			result.in(0).register,
			result.in(1).register,
			result.in(2).register);
		
		// Clear references
		this.__refClear();
	}
	
	/**
	 * Throws exception.
	 *
	 * @since 2019/03/26
	 */
	private final void __runAThrow()
	{
		// This operation throws an exception, so we will just go to checking
		// it.
		this._exceptioncheck = true;
		
		// Pop from the stack
		JavaStackResult result = this._stack.doStack(1);
		this._stack = result.after();
		
		// Enqueue?
		boolean doenq = this.__refEnqueue(result.enqueue());
		
		// Cannot be null
		RegisterCodeBuilder codebuilder = this.codebuilder;
		codebuilder.add(RegisterOperationType.IFNULL_REF_CLEAR,
			result.in(0).register, this.__makeException(
			new ClassName("java/lang/NullPointerException")));
		
		// Generate code
		codebuilder.add(RegisterOperationType.SET_AND_FLAG_EXCEPTION,
			result.in(0).register);
		
		// Clear references
		if (doenq)
			this.__refClear();
	}
	
	/**
	 * Read a value from a field.
	 *
	 * @param __fr The field to read from.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/03/26
	 */
	private final void __runGetField(FieldReference __fr)
		throws NullPointerException
	{
		if (__fr == null)
			throw new NullPointerException("NARG");
		
		// The data type determines which instruction to use
		PrimitiveType pt = __fr.memberType().primitiveType();
		DataType dt = DataType.of(pt);
		
		// Field access information
		AccessedField ac = this.__fieldAccess(FieldAccessType.INSTANCE, __fr);
		
		// Do stack operations
		JavaStackResult result = this._stack.doStack(1,
			new JavaType(__fr.memberType()));
		this._stack = result.after();
		
		// Enqueue instance possibly
		this.__refEnqueue(result.enqueue());
		
		// Cannot be null
		RegisterCodeBuilder codebuilder = this.codebuilder;
		codebuilder.add(RegisterOperationType.IFNULL_REF_CLEAR,
			result.in(0).register, this.__makeException(
			new ClassName("java/lang/NullPointerException")));
		
		// Generate code
		codebuilder.add(dt.fieldAccessOperation(false, false),
			ac,
			result.in(0).register,
			result.out(0).register);
		
		// Sign-extend signed types?
		if (pt == PrimitiveType.BYTE || pt == PrimitiveType.SHORT)
			codebuilder.add((pt == PrimitiveType.BYTE ?
					RegisterOperationType.SIGN_X8 :
					RegisterOperationType.SIGN_X16),
				result.out(0).register);
		
		// Clear references
		this.__refClear();
	}
	
	/**
	 * Jumps to another location.
	 *
	 * @param __t The target.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/04/02
	 */
	private final void __runGoto(InstructionJumpTarget __t)
		throws NullPointerException
	{
		if (__t == null)
			throw new NullPointerException("NARG");
		
		// Gotos are just basic jumps to a label
		this.codebuilder.add(RegisterOperationType.JUMP,
			this.__javaLabel(__t.target()));
	}
	
	/**
	 * Compares against zero.
	 *
	 * @param __ref Are these references?
	 * @param __ct The comparison type to perform.
	 * @param __j The jump target.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/03/26
	 */
	private final void __runIfZero(boolean __ref, CompareType __ct,
		InstructionJumpTarget __j)
		throws NullPointerException
	{
		if (__ct == null || __j == null)
			throw new NullPointerException("NARG");
		
		// Pop input from the stack
		JavaStackResult result = this._stack.doStack(1);
		this._stack = result.after();
		
		// Enqueue the input for counting
		boolean doenq = this.__refEnqueue(result.enqueue());
		
		// Generate code, no later refclear needs to be done because if
		// zero operation if doenq is set will clear the references
		this.codebuilder.add(__ct.ifZeroOperation(doenq),
			result.in(0).register, this.__javaLabel(__j.target()));
	}
	
	/**
	 * Handles invocation of other methods.
	 *
	 * @param __t The type of invocation to perform.
	 * @param __r The method to invoke.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/03/20
	 */
	private final void __runInvoke(InvokeType __t, MethodReference __r)
		throws NullPointerException
	{
		if (__t == null || __r == null)
			throw new NullPointerException("NARG");
		
		// The invoked method may throw an exception
		this._exceptioncheck = true;
		
		// Return value type, if any
		MethodHandle mf = __r.handle();
		FieldDescriptor rv = mf.descriptor().returnValue();
		boolean hasrv = (rv != null);
		
		// Number of argument to pop
		int popcount = mf.javaStack(__t.hasInstance()).length;
		
		// Perform stack operation
		JavaStackResult result = (!hasrv ? this._stack.doStack(popcount) :
			this._stack.doStack(popcount, new JavaType(rv)));
		this._stack = result.after();
		
		// Enqueue the input for counting
		this.__refEnqueue(result.enqueue());
		
		// Cannot be null if an instance type
		RegisterCodeBuilder codebuilder = this.codebuilder;
		if (__t.hasInstance())
			codebuilder.add(RegisterOperationType.IFNULL_REF_CLEAR,
				result.in(0).register, this.__makeException(
				new ClassName("java/lang/NullPointerException")));
		
		// Setup registers to use for the method call
		List<Integer> callargs = new ArrayList<>(popcount);
		for (int i = 0; i < popcount; i++)
		{
			// Add the input register
			JavaStackResult.Input in = result.in(i);
			callargs.add(in.register);
			
			// But also if it is wide, we need to pass the other one or else
			// the value will be clipped
			if (in.type.isWide())
				callargs.add(in.register + 1);
		}
		
		// Generate the call, pass the base register and the number of
		// registers to pass to the target method
		codebuilder.add(RegisterOperationType.INVOKE_METHOD,
			new InvokedMethod(__t, __r.handle()), new RegisterList(callargs));
		
		// Uncount any used references
		this.__refClear();
		
		// Load the return value onto the stack
		if (hasrv)
			throw new todo.TODO();
	}
	
	/**
	 * Load of constant value.
	 *
	 * @param __v The value to push.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/03/17
	 */
	private final void __runLdc(ConstantValue __v)
		throws NullPointerException
	{
		if (__v == null)
			throw new NullPointerException("NARG");
		
		// Get push properties
		JavaType jt = __v.type().javaType();
		
		// Push to the stack this type
		JavaStackResult result = this._stack.doStack(0, true, jt);
		this._stack = result.after();
		
		// Generate instruction
		RegisterCodeBuilder codebuilder = this.codebuilder;
		switch (__v.type())
		{
			case INTEGER:
				codebuilder.add(RegisterOperationType.X32_CONST,
					(Integer)__v.boxedValue(),
					result.out(0).register);
				break;
				
			case FLOAT:
				codebuilder.add(RegisterOperationType.X32_CONST,
					Float.floatToRawIntBits((Float)__v.boxedValue()),
					result.out(0).register);
				break;
			
			case LONG:
				codebuilder.add(RegisterOperationType.X64_CONST,
					__v.boxedValue(),
					result.out(0).register);
				break;
				
			case DOUBLE:
				codebuilder.add(RegisterOperationType.X64_CONST,
					Double.doubleToRawLongBits((Double)__v.boxedValue()),
					result.out(0).register);
				break;
			
			case STRING:
			case CLASS:
				codebuilder.add(RegisterOperationType.LOAD_POOL_VALUE,
					__v.boxedValue(),
					result.out(0).register);
				break;
			
			default:
				throw new todo.OOPS();
		}
	}
	
	/**
	 * Loads from a local onto the stack.
	 *
	 * @param __l The local to load.
	 * @since 2019/03/14
	 */
	private final void __runLoad(int __l)
	{
		// Just before the load
		JavaStackResult result = this._stack.doLocalLoad(__l);
		this._stack = result.after();
	}
	
	/**
	 * Runs the given math operation (c = a + b).
	 *
	 * @param __op The operation to use.
	 * @param __t The type on the stack.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/04/02
	 */
	private final void __runMathABC(int __op, JavaType __t)
		throws NullPointerException
	{
		if (__t == null)
			throw new NullPointerException("NARG");
		
		// Pop two values to operate and push one
		JavaStackResult result = this._stack.doStack(2, __t);
		this._stack = result.after();
		
		// Perform the work
		this.codebuilder.add(__op,
			result.in(0).register,
			result.in(1).register,
			result.out(0).register);
	}
	
	/**
	 * Handles class allocation.
	 *
	 * New is intended to be replaced by a special method call by the compiler
	 * or VM.
	 *
	 * @param __cn The class to allocate.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/03/24
	 */
	private final void __runNew(ClassName __cn)
		throws NullPointerException
	{
		if (__cn == null)
			throw new NullPointerException("NARG");
		
		// New is a complex operation and could fail for many reasons
		this._exceptioncheck = true;
		
		// Just the type is pushed to the stack
		JavaStackResult result = this._stack.doStack(0, new JavaType(__cn));
		this._stack = result.after();
		
		// Allocate and store into register
		RegisterCodeBuilder codebuilder = this.codebuilder;
		codebuilder.add(RegisterOperationType.NEW,
			__cn, result.out(0).register);
			
		// If null is returned then out of memory
		// Disabled since this should be thrown by the implementation of NEW
		// which should be like a static method call to an allocator
		if (false)
			codebuilder.add(RegisterOperationType.IFNULL,
				result.out(0).register, this.__makeException(
				new ClassName("java/lang/OutOfMemoryError")));
	}
	
	/**
	 * Allocate a new array.
	 *
	 * @param __t The type of array to create.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/03/24
	 */
	private final void __runNewArray(ClassName __t)
		throws NullPointerException
	{
		if (__t == null)
			throw new NullPointerException("NARG");
		
		// Allocation may fail or the class could be invalid
		this._exceptioncheck = true;
		
		// Only the length is on the stack
		JavaStackResult result = this._stack.doStack(1,
			new JavaType(__t.addDimensions(1)));
		this._stack = result.after();
		
		// Cannot be negative
		RegisterCodeBuilder codebuilder = this.codebuilder;
		codebuilder.add(RegisterOperationType.IFLT_REF_CLEAR,
			result.in(0).register, this.__makeException(
			new ClassName("java/lang/NegativeArraySizeException")));
		
		// Generate
		codebuilder.add(RegisterOperationType.NEW_ARRAY,
			__t, result.in(0).register, result.out(0).register);
		
		// If null is returned then out of memory
		codebuilder.add(RegisterOperationType.IFNULL,
			result.out(0).register, this.__makeException(
			new ClassName("java/lang/OutOfMemoryError")));
	}
	
	/**
	 * Puts a value into a field.
	 *
	 * @param __fr The field to put into.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/03/24
	 */
	private final void __runPutField(FieldReference __fr)
		throws NullPointerException
	{
		if (__fr == null)
			throw new NullPointerException("NARG");
		
		// [inst, value] ->
		JavaStackResult result = this._stack.doStack(2);
		this._stack = result.after();
		
		// The input may have been wiped
		this.__refEnqueue(result.enqueue());
		
		// Cannot be null
		RegisterCodeBuilder codebuilder = this.codebuilder;
		codebuilder.add(RegisterOperationType.IFNULL_REF_CLEAR,
			result.in(0).register, this.__makeException(
			new ClassName("java/lang/NullPointerException")));
		
		// Generate code
		codebuilder.add(
			DataType.of(__fr.memberType().primitiveType()).
				fieldAccessOperation(false, true),
			this.__fieldAccess(FieldAccessType.INSTANCE, __fr),
			result.in(0).register,
			result.in(1).register);
		
		// Clear references as needed
		this.__refClear();
	}
	
	/**
	 * Handles method return.
	 *
	 * @param __dr Is something to be returned?
	 * @since 2019/03/22
	 */
	private final void __runReturn(boolean __dr)
	{
		// Destroy the entire stack
		JavaStackResult result = this._stack.doDestroy(__dr);
		this._stack = result.after();
		
		// If returning a value, load it into the return register
		if (__dr)
		{
			RegisterCodeBuilder codebuilder = this.codebuilder;
			
			throw new todo.TODO();
		}
		
		// Generate or jump to return which has this enqueue state
		this.__return(result.enqueue());
	}
	
	/**
	 * Performs stack shuffling.
	 *
	 * @param __jsst The type of shuffle to perform.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/03/24
	 */
	private final void __runShuffle(JavaStackShuffleType __jsst)
		throws NullPointerException
	{
		this._stack = this._stack.doStackShuffle(__jsst).after();
	}
	
	/**
	 * Stores stack value into a local.
	 *
	 * @param __l The value to store.
	 * @since 2019/04/01
	 */
	private final void __runStore(int __l)
	{
		JavaStackResult result = this._stack.doLocalStore(__l);
		this._stack = result.after();
		
		// The destination potentially is being overwritten, so uncount it
		RegisterCodeBuilder codebuilder = this.codebuilder;
		JavaStackEnqueueList enq = result.enqueue();
		if (!enq.isEmpty())
			for (int i = 0, n = enq.size(); i < n; i++)
				codebuilder.add(RegisterOperationType.UNCOUNT,
					enq.get(i));
		
		// Variables being touched
		JavaStackResult.Input in = result.in(0);
		JavaStackResult.Output out = result.out(0);
		
		// Just do a plain copy, there is no need to perform counting on it
		// because the stack would uncount and then the local would count so
		// there would be a net zero count
		codebuilder.add((in.type.isWide() ? RegisterOperationType.X64_COPY :
			RegisterOperationType.X32_COPY), in.register, out.register);
	}
}
