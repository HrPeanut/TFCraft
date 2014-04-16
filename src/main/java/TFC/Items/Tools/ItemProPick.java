package TFC.Items.Tools;

import java.util.HashMap;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import TFC.Reference;
import TFC.TFCBlocks;
import TFC.API.Enums.EnumItemReach;
import TFC.API.Enums.EnumSize;
import TFC.API.Enums.EnumWeight;
import TFC.Core.TFCTabs;
import TFC.Core.TFC_Textures;
import TFC.Items.ItemTerra;

public class ItemProPick extends ItemTerra
{
	HashMap<String, ProspectResult> results =
			new HashMap<String, ProspectResult>();
	Random random = null;

	public ItemProPick() 
	{
		super();
		maxStackSize = 1;
		setCreativeTab(TFCTabs.TFCTools);
		this.setWeight(EnumWeight.LIGHT);
		this.setSize(EnumSize.SMALL);
	}

	@Override
	public void registerIcons(IIconRegister registerer)
	{
		this.itemIcon = registerer.registerIcon(Reference.ModID + ":" + "tools/"+this.getUnlocalizedName().replace("item.", ""));
	}

	@Override
	public IIcon getIcon(ItemStack stack, int pass)
	{
		NBTTagCompound nbt = stack.getTagCompound();
		if(pass == 1 && nbt != null && nbt.hasKey("broken"))
			return TFC_Textures.BrokenItem;
		else
			return getIconFromDamageForRenderPass(stack.getItemDamage(), pass);
	}

	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		Block block = world.getBlock(x, y, z);

		// Negated the old condition and exiting the method here instead.
		if (block == TFCBlocks.ToolRack)
			return true;

		// Getting the meta data only when we actually need it.
		int meta = world.getBlockMetadata(x, y, z);

		// Damage the item on prospecting use.
		if (!world.isRemote) {
			itemStack.damageItem(1, player);
			if (itemStack.getItemDamage() >= itemStack.getMaxDamage())
				player.destroyCurrentEquippedItem();

			return true;
		}

		// If an ore block is targeted directly, it'll tell you what it is.
		if (block == TFCBlocks.Ore ||
			block == TFCBlocks.Ore2 ||
			block == TFCBlocks.Ore3)
		{
			TellResult(player, new ItemStack(block, 1, meta));
			return true;
		}

		random = new Random(x * z + y);

		// If random(100) is less than 60, it used to succeed. we don't need to
		// gather the blocks in a 25x25 area if it doesn't.
		if (random.nextInt(100) >= 60) {
			player.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("gui.ProPick.FoundNothing")));
			return true;
		}

		// Check all blocks in the 25x25 area, centered on the targeted block.
		for (int i = -12; i < 12; i++)
			for (int j = -12; j < 12; j++)
				for(int k = -12; k < 12; k++) {
					int blockX = x + i, 
							blockY = y + j,
							blockZ = z + k;

					block = world.getBlock(blockX, blockY, blockZ);

					if (block != TFCBlocks.Ore &&
						block != TFCBlocks.Ore2 &&
						block != TFCBlocks.Ore3)
						continue;

					meta = world.getBlockMetadata(blockX, blockY, blockZ);
					ItemStack ore = new ItemStack(block, 1, meta);
					String oreName = ore.getDisplayName();

					if (results.containsKey(oreName))
						results.get(oreName).Count++;
					else
						results.put(oreName, new ProspectResult(ore, 1));

					ore = null;
					oreName = null;
				}

		// Tell the player what was found.
		TellResult(player);

		results.clear();
		random = null;

		return true;
	}

	/*
	 * Tells the player what block of ore he found, when directly targeting an ore block.
	 */
	private void TellResult(EntityPlayer player, ItemStack ore) {
		player.addChatMessage(new ChatComponentText(String.format("%s %s", StatCollector.translateToLocal("gui.ProPick.Found"), ore.getItem().getItemStackDisplayName(ore))));
	}

	/*
	 * Tells the player what ore has been found, randomly picked off the HashMap.
	 */
	private void TellResult(EntityPlayer player) {
		if (results == null || results.size() == 0) {
			player.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("gui.ProPick.FoundNothing")));
			return;
		}

		int index = random.nextInt(results.size());
		ProspectResult result = results.values().toArray(new ProspectResult[0])[index];
		String oreName = result.ItemStack.getItem().getItemStackDisplayName(result.ItemStack);

		if (result.Count < 10)
			player.addChatMessage(new ChatComponentText(String.format("%s %s", StatCollector.translateToLocal("gui.ProPick.FoundTraces"), oreName)));
		else if(result.Count < 20)
			player.addChatMessage(new ChatComponentText(String.format("%s %s", StatCollector.translateToLocal("gui.ProPick.FoundSmall"), oreName)));
		else if (result.Count < 40)
			player.addChatMessage(new ChatComponentText(String.format("%s %s", StatCollector.translateToLocal("gui.ProPick.FoundMedium"), oreName)));
		else if (result.Count < 80)
			player.addChatMessage(new ChatComponentText(String.format("%s %s", StatCollector.translateToLocal("gui.ProPick.FoundLarge"), oreName)));
		else
			player.addChatMessage(new ChatComponentText(String.format("%s %s", StatCollector.translateToLocal("gui.ProPick.FoundVeryLarge"), oreName)));

		oreName = null;
		result = null;
	}

	@Override
	public boolean canStack() {
		// TODO Auto-generated method stub
		return false;
	}

	private class ProspectResult {
		public ItemStack ItemStack;
		public int Count;

		public ProspectResult(ItemStack itemStack, int count) {
			ItemStack = itemStack;
			Count = count;
		}
	}
	
	@Override
	public EnumItemReach getReach(ItemStack is){
		return EnumItemReach.SHORT;
	}
}