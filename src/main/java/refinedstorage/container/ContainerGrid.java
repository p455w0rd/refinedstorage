package refinedstorage.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import refinedstorage.block.EnumGridType;
import refinedstorage.container.slot.SlotGridCraftingResult;
import refinedstorage.tile.TileGrid;

public class ContainerGrid extends ContainerBase
{
	private TileGrid grid;

	public ContainerGrid(EntityPlayer player, TileGrid grid)
	{
		super(player);

		this.grid = grid;

		addPlayerInventory(8, grid.getType() == EnumGridType.CRAFTING ? 174 : 108);

		if (grid.getType() == EnumGridType.CRAFTING)
		{
			int x = 25;
			int y = 106;

			for (int i = 0; i < 9; ++i)
			{
				addSlotToContainer(new Slot(grid.getCraftingInventory(), i, x, y));

				x += 18;

				if ((i + 1) % 3 == 0)
				{
					y += 18;
					x = 25;
				}
			}

			addSlotToContainer(new SlotGridCraftingResult(player, grid.getCraftingInventory(), grid.getCraftingResultInventory(), grid, 0, 133 + 4, 120 + 4));
		}
	}
}