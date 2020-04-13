package com.lukeneedham.minecartcoupling.common.utils;

import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class InvTools {

    public static boolean isEmpty(@Nullable ItemStack stack) {
        return stack == null || stack.isEmpty();
    }

    public static ItemStack depleteItem(ItemStack stack) {
        if (sizeOf(stack) == 1)
            return stack.getItem().getContainerItem(stack);
        else {
            stack.splitStack(1);
            return stack;
        }
    }

    public static int sizeOf(ItemStack stack) {
        if (isEmpty(stack))
            return 0;
        return stack.getCount();
    }
}
