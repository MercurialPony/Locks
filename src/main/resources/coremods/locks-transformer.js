function initializeCoreMod()
{
	// Imports
	var Opcodes = Java.type("org.objectweb.asm.Opcodes");
	var FieldNode = Java.type("org.objectweb.asm.tree.FieldNode");
	var IntInsnNode = Java.type("org.objectweb.asm.tree.IntInsnNode");
	var FieldInsnNode = Java.type("org.objectweb.asm.tree.FieldInsnNode");
	var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
	return {
		"ServerWorld#notifyBlockUpdate":
		{
			target:
			{
				type: "METHOD",
				class: "net.minecraft.world.server.ServerWorld",
				methodName: "func_184138_a",
				methodDesc: "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;I)V"
			},
			transformer: function(node)
			{
				// We want to insert our code at the start of the method
				// Add instructions in reverse order, because insert adds to the start of the instruction list
				// Lastly, invoke our method with all of the parameters from the stack
				node.instructions.insert(new MethodInsnNode(Opcodes.INVOKESTATIC, "melonslise/locks/coremod/LocksDelegates", "onBlockUpdate", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;I)V"));
				// Push the 4th local variable to the stack. Should be an integer
				node.instructions.insert(new IntInsnNode(Opcodes.ILOAD, 4));
				// Push the 3rd local variable to the stack. Should be a BlockState object
				node.instructions.insert(new IntInsnNode(Opcodes.ALOAD, 3));
				// Push the 2nd local variable to the stack. Should be a BlockState object
				node.instructions.insert(new IntInsnNode(Opcodes.ALOAD, 2));
				// Push the 1st local variable to the stack. Should be a BlockPos object
				node.instructions.insert(new IntInsnNode(Opcodes.ALOAD, 1));
				// Push the class instance to the stack
				node.instructions.insert(new IntInsnNode(Opcodes.ALOAD, 0));
				return node;
			}
		},
		"Template":
		{
			target:
			{
				type: "CLASS",
				name: "net.minecraft.world.gen.feature.template.Template"
			},
			transformer: function(node)
			{
				// Add private final field "lockables" of type List and generic type LockableInfo
				node.fields.add(new FieldNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "lockables", "Ljava/util/List;", "Ljava/util/List<Lmelonslise/locks/coremod/LockableInfo;>;", null));
				return node;
			}
		},
		"Template#takeBlocksFromWorld":
		{
			target:
			{
				type: "METHOD",
				class: "net.minecraft.world.gen.feature.template.Template",
				methodName: "func_186254_a",
				methodDesc: "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;ZLnet/minecraft/block/Block;)V"
			},
			transformer: function(node)
			{
				var counter = 0;
				// We want to insert our code at the start of the the first if-block
				for(var iterator = node.instructions.iterator(); iterator.hasNext();)
				{
					var instruction = iterator.next();
					// Look for the third if instruction
					if(instruction.getOpcode() !== Opcodes.IF_ICMPLT)
						continue;
					++counter;
					if(counter !== 3)
						continue;
					// Add our instructions after the if instruction in reverse order
					// Assign the result of our method invokation to our field
					node.instructions.insert(instruction, new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/world/gen/feature/template/Template", "lockables", "Ljava/util/List;"))
					// Invoke our method with the params loaded into the stack
					node.instructions.insert(instruction, new MethodInsnNode(Opcodes.INVOKESTATIC, "melonslise/locks/coremod/LocksDelegates", "takeLockablesFromWorld", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Ljava/util/List;"));
					// Push the 3rd local variable to the stack. Should be a BlockPos
					node.instructions.insert(instruction, new IntInsnNode(Opcodes.ALOAD, 3));
					// Push the 2nd local variable to the stack. Should be a BlockPos
					node.instructions.insert(instruction, new IntInsnNode(Opcodes.ALOAD, 2));
					// Push the 1st local variable to the stack. Should be a World
					node.instructions.insert(instruction, new IntInsnNode(Opcodes.ALOAD, 1));
					// Push the class instance to the stack
					node.instructions.insert(instruction, new IntInsnNode(Opcodes.ALOAD, 0));
					break;
				}
				return node;
			}
		},
		"Template#addBlocksToWorld":
		{
			target:
			{
				type: "METHOD",
				class: "net.minecraft.world.gen.feature.template.Template",
				methodName: "func_189962_a",
				methodDesc: "(Lnet/minecraft/world/IWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/gen/feature/template/PlacementSettings;I)Z"
			},
			transformer: function(node)
			{
				// We want to insert our code before the return true instruction
				for(var iterator = node.instructions.iterator(); iterator.hasNext();)
				{
					var instruction = iterator.next();
					// Look for an instruction which pushes 1 to the stack followed by a return
					if(instruction.getOpcode() !== Opcodes.ICONST_1 || node.instructions.get(node.instructions.indexOf(instruction) + 1).getOpcode() !== Opcodes.IRETURN)
						continue;
					// Push the class instance to the stack
					node.instructions.insertBefore(instruction, new IntInsnNode(Opcodes.ALOAD, 0));
					// Push our field to the stack
					node.instructions.insertBefore(instruction, new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/world/gen/feature/template/Template", "lockables", "Ljava/util/List;"));
					// Push the 1st local variable to the stack. Should be an IWorld
					node.instructions.insertBefore(instruction, new IntInsnNode(Opcodes.ALOAD, 1));
					// Push the 2nd local variable to the stack. Should be a BlockPos
					node.instructions.insertBefore(instruction, new IntInsnNode(Opcodes.ALOAD, 2));
					// Push the 3rd local variable to the stack. Should be a PlacementSettings
					node.instructions.insertBefore(instruction, new IntInsnNode(Opcodes.ALOAD, 3));
					// Invoke our method with the params loaded into the stack
					node.instructions.insertBefore(instruction, new MethodInsnNode(Opcodes.INVOKESTATIC, "melonslise/locks/coremod/LocksDelegates", "addLockablesToWorld", "(Ljava/util/List;Lnet/minecraft/world/IWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/gen/feature/template/PlacementSettings;)V"));
					break;
				}
				return node;
			}
		},
		"Template#writeToNBT":
		{
			target:
			{
				type: "METHOD",
				class: "net.minecraft.world.gen.feature.template.Template",
				methodName: "func_189552_a",
				methodDesc: "(Lnet/minecraft/nbt/CompoundNBT;)Lnet/minecraft/nbt/CompoundNBT;"
			},
			transformer: function(node)
			{
				// We want to insert our code at the start of the method
				// Add instructions in reverse order, because insert adds to the start of the instruction list
				// Invoke our method with the params loaded into the stack
				node.instructions.insert(new MethodInsnNode(Opcodes.INVOKESTATIC, "melonslise/locks/coremod/LocksDelegates", "writeLockablesToNBT", "(Lnet/minecraft/nbt/CompoundNBT;Ljava/util/List;)V"));
				// Push our field to the stack
				node.instructions.insert(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/world/gen/feature/template/Template", "lockables", "Ljava/util/List;"));
				// Push the class instance to the stack
				node.instructions.insert(new IntInsnNode(Opcodes.ALOAD, 0));
				// Push the 1st local variable to the stack. Should be a CompoundNBT
				node.instructions.insert(new IntInsnNode(Opcodes.ALOAD, 1));
				return node;
			}
		},
		"Template#read":
		{
			target:
			{
				type: "METHOD",
				class: "net.minecraft.world.gen.feature.template.Template",
				methodName: "func_186256_b",
				methodDesc: "(Lnet/minecraft/nbt/CompoundNBT;)V"
			},
			transformer: function(node)
			{
				// We want to insert our code at the start of the method
				// Add instructions in reverse order, because insert adds to the start of the instruction list
				// Assign the result of our method invokation to our field
				node.instructions.insert(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/world/gen/feature/template/Template", "lockables", "Ljava/util/List;"))
				// Invoke our method with the params loaded into the stack
				node.instructions.insert(new MethodInsnNode(Opcodes.INVOKESTATIC, "melonslise/locks/coremod/LocksDelegates", "readLockablesFromNBT", "(Lnet/minecraft/nbt/CompoundNBT;)Ljava/util/List;"));
				// Push the 1st local variable to the stack. Should be a CompoundNBT
				node.instructions.insert(new IntInsnNode(Opcodes.ALOAD, 1));
				// Push the class instance to the stack
				node.instructions.insert(new IntInsnNode(Opcodes.ALOAD, 0));
				return node;
			}
		}
	};
}

/*
"EditStructureScreen":
{
	target:
	{
		type: "CLASS",
		name: "net.minecraft.client.gui.screen.EditStructureScreen"
	},
	transformer: function(node)
	{
		// Import operation codes
		var Opcodes = Java.type("org.objectweb.asm.Opcodes");
		// Import field node
		var FieldNode = Java.type("org.objectweb.asm.tree.FieldNode");

		node.fields.add(new FieldNode(Opcodes.ACC_FINAL, "randomizeLocksButton", "Lnet/minecraft/client/gui/widget/button/Button;", null, null));
	}
},
"EditStructureScreen#init":
{
	target:
	{
		type: "METHOD",
		class: "net.minecraft.client.gui.screen.EditStructureScreen",
		methodName: "init",
		methodDesc: "()V"
	},
	transformer: function(node)
	{
		// Import operation codes
		var Opcodes = Java.type("org.objectweb.asm.Opcodes");

	}
},
*/
