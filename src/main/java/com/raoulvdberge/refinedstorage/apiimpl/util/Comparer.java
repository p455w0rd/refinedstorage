package com.raoulvdberge.refinedstorage.apiimpl.util;

import com.raoulvdberge.refinedstorage.api.util.IComparer;
import com.raoulvdberge.refinedstorage.apiimpl.API;
import com.raoulvdberge.refinedstorage.block.BlockNode;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class Comparer implements IComparer {
    private Map<Integer, Boolean> oredictCache = new HashMap<>();

    @Override
    public boolean isEqual(@Nullable ItemStack left, @Nullable ItemStack right, int flags) {
        EnumActionResult validity = validityCheck(left, right);

        if (validity == EnumActionResult.FAIL || validity == EnumActionResult.SUCCESS) {
            return validity == EnumActionResult.SUCCESS;
        }

        if ((flags & COMPARE_OREDICT) == COMPARE_OREDICT) {
            if (isEqualOredict(left, right)) {
                return true;
            }
        }

        if (left.getItem() != right.getItem()) {
            return false;
        }

        if ((flags & COMPARE_DAMAGE) == COMPARE_DAMAGE && left.getItemDamage() != OreDictionary.WILDCARD_VALUE && right.getItemDamage() != OreDictionary.WILDCARD_VALUE) {
            if (left.getItemDamage() != right.getItemDamage()) {
                return false;
            }
        }

        if ((flags & COMPARE_NBT) == COMPARE_NBT) {
            if ((flags & COMPARE_STRIP_NBT) == COMPARE_STRIP_NBT) {
                left = stripTags(left.copy());
                right = stripTags(right.copy());
            }
            if (!isEqualNBT(left, right)) {
                return false;
            }
        }

        if ((flags & COMPARE_QUANTITY) == COMPARE_QUANTITY) {
            if (left.getCount() != right.getCount()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean isEqual(@Nullable FluidStack left, @Nullable FluidStack right, int flags) {
        if (left == null && right == null) {
            return true;
        }

        if ((left == null && right != null) || (left != null && right == null)) {
            return false;
        }

        if (left.getFluid() != right.getFluid()) {
            return false;
        }

        if ((flags & COMPARE_QUANTITY) == COMPARE_QUANTITY) {
            if (left.amount != right.amount) {
                return false;
            }
        }

        if ((flags & COMPARE_NBT) == COMPARE_NBT) {
            if (left.tag != null && !left.tag.equals(right.tag)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean isEqualNBT(@Nullable ItemStack left, @Nullable ItemStack right) {
        EnumActionResult validity = validityCheck(left, right);

        if (validity == EnumActionResult.FAIL || validity == EnumActionResult.SUCCESS) {
            return validity == EnumActionResult.SUCCESS;
        }

        if (!ItemStack.areItemStackTagsEqual(left, right)) {
            if (left.hasTagCompound() && !right.hasTagCompound() && left.getTagCompound().hasNoTags()) {
                return true;
            } else if (!left.hasTagCompound() && right.hasTagCompound() && right.getTagCompound().hasNoTags()) {
                return true;
            }

            return false;
        }

        return true;
    }

    @Override
    public boolean isEqualOredict(@Nullable ItemStack left, @Nullable ItemStack right) {
        EnumActionResult validity = validityCheck(left, right);

        if (validity == EnumActionResult.FAIL || validity == EnumActionResult.SUCCESS) {
            return validity == EnumActionResult.SUCCESS;
        }

        int code = API.instance().getItemStackHashCode(left);
        code = 31 * code + API.instance().getItemStackHashCode(right);

        if (oredictCache.containsKey(code)) {
            return oredictCache.get(code);
        }

        int[] leftIds = OreDictionary.getOreIDs(left);
        int[] rightIds = OreDictionary.getOreIDs(right);

        for (int i : rightIds) {
            if (ArrayUtils.contains(leftIds, i)) {
                oredictCache.put(code, true);

                return true;
            }
        }

        oredictCache.put(code, false);

        return false;
    }

    private EnumActionResult validityCheck(@Nullable ItemStack left, @Nullable ItemStack right) {
        if (left == null && right == null) {
            return EnumActionResult.SUCCESS;
        }

        if ((left == null && right != null) || (left != null && right == null)) {
            return EnumActionResult.FAIL;
        }

        if (left.isEmpty() && right.isEmpty()) {
            return EnumActionResult.SUCCESS;
        }

        if ((left.isEmpty() && !right.isEmpty()) || (!left.isEmpty() && right.isEmpty())) {
            return EnumActionResult.FAIL;
        }

        return EnumActionResult.PASS;
    }

    public static ItemStack stripTags(ItemStack stack) {
        if (stack != null && stack.hasTagCompound()) {
            switch (stack.getItem().getRegistryName().getResourceDomain()) {
                case "mekanism":
                    stack.getTagCompound().removeTag("mekData");
                    break;
                case "enderio":
                    // Soul vials
                    stack.getTagCompound().removeTag("entity");
                    stack.getTagCompound().removeTag("isStub");
                    // Capacitors
                    stack.getTagCompound().removeTag("Energy");
                    break;
                case "minecraft":
                    stack.getTagCompound().removeTag("RepairCost");
                    break;
                case "simplyjetpacks":
                    stack.getTagCompound().removeTag("sjData");
                    stack.getTagCompound().removeTag("PackOn");
                    break;
                case "refinedstorage":
                    stack.getTagCompound().removeTag(BlockNode.NBT_REFINED_STORAGE_DATA);
                    break;
            }
        }

        return stack;
    }
}
