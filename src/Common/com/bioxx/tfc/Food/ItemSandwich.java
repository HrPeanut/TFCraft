package com.bioxx.tfc.Food;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;

import com.bioxx.tfc.Core.Player.FoodStatsTFC;

public class ItemSandwich extends ItemMeal
{

	public ItemSandwich()
	{
		super();
		this.hasSubtypes = true;
		this.MetaNames = new String[]{"Sandwich Wheat","Sandwich Oat","Sandwich Barley","Sandwich Rye","Sandwich Corn","Sandwich Rice"};
		this.MetaIcons = new IIcon[6];
		this.setFolder("food/");
	}

	@Override
	protected void addFGInformation(ItemStack is, List arraylist)
	{
		if (is.hasTagCompound())
		{
			NBTTagCompound nbt = is.getTagCompound();
			if(nbt.hasKey("FG"))
			{
				int[] fg = nbt.getIntArray("FG");
				for(int i = 0; i < fg.length; i++)
				{
					if(i == 5 && fg[5] == fg[0])
						return;
					if(fg[i] != -1)
						arraylist.add(localize(fg[i]));
				}
			}
		}
	}

	@Override
	protected float getEatAmount(FoodStatsTFC fs, float amount)
	{
		float eatAmount = Math.min(amount, 10);
		float stomachDiff = fs.stomachLevel+eatAmount-fs.getMaxStomach(fs.player);
		if(stomachDiff > 0)
			eatAmount-=stomachDiff;
		return eatAmount;
	}

	@Override
	protected float getFillingBoost()
	{
		return 1.25f;
	}

	@Override
	protected float[] getFoodWeights()
	{
		return new float[]{2f,3f,2f,2f,1f};
	}

	@Override
	public float getFoodMaxWeight(ItemStack is) {
		return 10;
	}

	@Override
	public boolean renderDecay() {
		return true;
	}

	@Override
	public boolean renderWeight() {
		return false;
	}
}
