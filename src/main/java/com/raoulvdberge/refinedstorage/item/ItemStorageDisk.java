package com.raoulvdberge.refinedstorage.item;

import com.raoulvdberge.refinedstorage.RSItems;
import com.raoulvdberge.refinedstorage.apiimpl.storage.StorageItemNBT;
import com.raoulvdberge.refinedstorage.block.EnumItemStorageType;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import java.util.Iterator;
import java.util.List;

public class ItemStorageDisk extends ItemBase {
    public static final int TYPE_1K = 0;
    public static final int TYPE_4K = 1;
    public static final int TYPE_16K = 2;
    public static final int TYPE_64K = 3;
    public static final int TYPE_CREATIVE = 4;
    public static final int TYPE_DEBUG = 5;

    private NBTTagCompound debugDiskTag;

    public ItemStorageDisk() {
        super("storage_disk");

        setMaxStackSize(1);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @Override
    public void getSubItems(Item item, CreativeTabs tab, NonNullList<ItemStack> subItems) {
        for (int i = 0; i < 5; ++i) {
            subItems.add(StorageItemNBT.createStackWithNBT(new ItemStack(item, 1, i)));
        }
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.onUpdate(stack, world, entity, slot, selected);

        if (!stack.hasTagCompound()) {
            if (stack.getItemDamage() == TYPE_DEBUG) {
                applyDebugDiskData(stack);
            } else {
                StorageItemNBT.createStackWithNBT(stack);
            }
        }
    }

    private void applyDebugDiskData(ItemStack stack) {
        if (debugDiskTag == null) {
            debugDiskTag = StorageItemNBT.createNBT();

            StorageItemNBT storage = new StorageItemNBT(debugDiskTag, -1, null) {
                @Override
                public int getPriority() {
                    return 0;
                }

                @Override
                public boolean isVoiding() {
                    return false;
                }
            };

            Iterator<Item> it = REGISTRY.iterator();

            while (it.hasNext()) {
                Item item = it.next();

                if (item != RSItems.STORAGE_DISK) {
                    NonNullList<ItemStack> stacks = NonNullList.create();

                    item.getSubItems(item, CreativeTabs.INVENTORY, stacks);

                    for (ItemStack itemStack : stacks) {
                        storage.insert(itemStack, 1000, false);
                    }
                }
            }

            storage.writeToNBT();
        }

        stack.setTagCompound(debugDiskTag.copy());
    }

    @Override
    public void addInformation(ItemStack disk, EntityPlayer player, List<String> tooltip, boolean advanced) {
        if (StorageItemNBT.isValid(disk)) {
            int capacity = EnumItemStorageType.getById(disk.getItemDamage()).getCapacity();

            if (capacity == -1) {
                tooltip.add(I18n.format("misc.refinedstorage:storage.stored", StorageItemNBT.getStoredFromNBT(disk.getTagCompound())));
            } else {
                tooltip.add(I18n.format("misc.refinedstorage:storage.stored_capacity", StorageItemNBT.getStoredFromNBT(disk.getTagCompound()), capacity));
            }
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack disk = player.getHeldItem(hand);

        if (!world.isRemote && player.isSneaking() && StorageItemNBT.isValid(disk) && StorageItemNBT.getStoredFromNBT(disk.getTagCompound()) <= 0 && disk.getMetadata() != TYPE_CREATIVE) {
            ItemStack storagePart = new ItemStack(RSItems.STORAGE_PART, 1, disk.getMetadata());

            if (!player.inventory.addItemStackToInventory(storagePart.copy())) {
                InventoryHelper.spawnItemStack(world, player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ(), storagePart);
            }

            return new ActionResult<>(EnumActionResult.SUCCESS, new ItemStack(RSItems.STORAGE_HOUSING));
        }

        return new ActionResult<>(EnumActionResult.PASS, disk);
    }

    @Override
    public void onCreated(ItemStack stack, World world, EntityPlayer player) {
        super.onCreated(stack, world, player);

        StorageItemNBT.createStackWithNBT(stack);
    }

    @Override
    public int getEntityLifespan(ItemStack stack, World world) {
        return Integer.MAX_VALUE;
    }

    @Override
    public NBTTagCompound getNBTShareTag(ItemStack stack) {
        return StorageItemNBT.getNBTShareTag(stack.getTagCompound());
    }
}
