package StevenDimDoors.mod_pocketDim.items;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import StevenDimDoors.mod_pocketDim.mod_pocketDim;
import StevenDimDoors.mod_pocketDim.blocks.BaseDimDoor;
import StevenDimDoors.mod_pocketDim.core.DimLink;
import StevenDimDoors.mod_pocketDim.core.LinkTypes;
import StevenDimDoors.mod_pocketDim.core.NewDimData;
import StevenDimDoors.mod_pocketDim.core.PocketManager;
import StevenDimDoors.mod_pocketDim.util.Point4D;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemRiftSignature extends Item
{
	public ItemRiftSignature(int itemID)
	{
		super(itemID);
		this.setMaxStackSize(1);
		this.setMaxDamage(0);
		this.hasSubtypes = true;
		this.setCreativeTab(mod_pocketDim.dimDoorsCreativeTab);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean hasEffect(ItemStack stack)
	{
		//Make the item glow if it has one endpoint stored
		return (stack.getItemDamage() != 0);
	}

	public void registerIcons(IconRegister par1IconRegister)
	{
		this.itemIcon = par1IconRegister.registerIcon(mod_pocketDim.modid + ":" + this.getUnlocalizedName());
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		// We must use onItemUseFirst() instead of onItemUse() because Minecraft checks
		// whether the user is in creative mode after calling onItemUse() and undoes any
		// damage we might set to indicate the rift sig has been activated. Otherwise,
		// we would need to rely on checking NBT tags for hasEffect() and that function
		// gets called constantly. Avoiding NBT lookups reduces our performance impact.

		// Return false on the client side to pass this request to the server
		if (world.isRemote)
		{
			return false;
		}
		
		y += 2; //Increase y by 2 to place the rift at head level
		if (!player.canPlayerEdit(x, y, z, side, stack))
		{
			return true;
		}
		int adjustedY = adjustYForSpecialBlocks(world,x,y,z);
		Point4DOrientation source = getSource(stack);
		int orientation = MathHelper.floor_double((double) ((player.rotationYaw + 180.0F) * 4.0F / 360.0F) - 0.5D) & 3;
		if (source != null)
		{
			//The link was used before and already has an endpoint stored. Create links connecting the two endpoints.
			NewDimData sourceDimension = PocketManager.getDimensionData(source.getDimension());
			NewDimData destinationDimension = PocketManager.getDimensionData(world);
			DimLink link = sourceDimension.createLink(source.getX(), source.getY(), source.getZ(), LinkTypes.NORMAL,source.getOrientation());
			DimLink reverse = destinationDimension.createLink(x, adjustedY, z, LinkTypes.NORMAL,orientation);
			destinationDimension.setDestination(link, x, adjustedY, z);
			sourceDimension.setDestination(reverse, source.getX(), source.getY(), source.getZ());

			//Try placing a rift at the destination point
			if (!mod_pocketDim.blockRift.isBlockImmune(world, x, adjustedY, z))
			{
				world.setBlock(x, adjustedY, z, mod_pocketDim.blockRift.blockID);
			}

			//Try placing a rift at the source point, but check if its world is loaded first
			World sourceWorld = DimensionManager.getWorld(sourceDimension.id());
			if (sourceWorld != null &&
				!mod_pocketDim.blockRift.isBlockImmune(sourceWorld, source.getX(), source.getY(), source.getZ()))
			{
				sourceWorld.setBlock(source.getX(), source.getY(), source.getZ(), mod_pocketDim.blockRift.blockID);
			}

			if (!player.capabilities.isCreativeMode)
			{
				stack.stackSize--;
			}
			clearSource(stack);
			mod_pocketDim.sendChat(player,("Rift Created"));
			world.playSoundAtEntity(player,mod_pocketDim.modid+":riftEnd", 0.6f, 1);
		}
		else
		{
			//The link signature has not been used. Store its current target as the first location. 
			setSource(stack, x, adjustedY, z,orientation, PocketManager.getDimensionData(world));
			mod_pocketDim.sendChat(player,("Location Stored in Rift Signature"));
			world.playSoundAtEntity(player,mod_pocketDim.modid+":riftStart", 0.6f, 1);
		}
		return true;
	}

	/**
	 * allows items to add custom lines of information to the mouseover description
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4)
	{
		Point4DOrientation source = getSource(par1ItemStack);
		if (source != null)
		{
			par3List.add("Leads to (" + source.getX() + ", " + source.getY() + ", " + source.getZ() + ") at dimension #" + source.getDimension());
		}
		else
		{
			par3List.add("First click stores a location;");
			par3List.add("second click creates a pair of");
			par3List.add("rifts linking the two locations.");
		}
	}

	/**
	 * Makes the rift placement account for replaceable blocks and doors.
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @return the adjusted y coord
	 */
	public static int adjustYForSpecialBlocks(World world, int x, int y, int z)
	{
		y=y-2;//get the block the player actually clicked on
		Block block = Block.blocksList[world.getBlockId(x, y, z)];
		if(block.isBlockReplaceable(world, x, y, z))
		{
			return y+1;//move block placement down (-2+1) one so its directly over things like snow
		}
		if(block instanceof BaseDimDoor)
		{
			if(world.getBlockId(x, y-1, z)==block.blockID&&world.getBlockMetadata(x, y, z)==8)
			{
				return y;//move rift placement down two so its in the right place on the door. 
			}
			return y+1;
		}
		return y+2;
	}
	public static void setSource(ItemStack itemStack, int x, int y, int z, int orientation, NewDimData dimension)
	{
		NBTTagCompound tag = new NBTTagCompound();

		tag.setInteger("linkX", x);
		tag.setInteger("linkY", y);
		tag.setInteger("linkZ", z);
		tag.setInteger("orientation", orientation);
		tag.setInteger("linkDimID", dimension.id());

		itemStack.setTagCompound(tag);
		itemStack.setItemDamage(1);
	}

	public static void clearSource(ItemStack itemStack)
	{
		//Don't just set the tag to null since there may be other data there (e.g. for renamed items)
		NBTTagCompound tag = itemStack.getTagCompound();
		tag.removeTag("linkX");
		tag.removeTag("linkY");
		tag.removeTag("linkZ");
		tag.removeTag("orientation");
		tag.removeTag("linkDimID");
		itemStack.setItemDamage(0);
	}

	public static Point4DOrientation getSource(ItemStack itemStack)
	{
		if (itemStack.getItemDamage() != 0)
		{
			if (itemStack.hasTagCompound())
			{
				NBTTagCompound tag = itemStack.getTagCompound();

				Integer x = tag.getInteger("linkX");
				Integer y = tag.getInteger("linkY");
				Integer z = tag.getInteger("linkZ");
				Integer orientation = tag.getInteger("orientation");
				Integer dimID = tag.getInteger("linkDimID");

				if (x != null && y != null && z != null && dimID != null)
				{
					return new Point4DOrientation(x, y, z,orientation, dimID);
				}
			}
			itemStack.setItemDamage(0);
		}
		return null;
	}
	
	static class Point4DOrientation
	{
		private Point4D point;
		private int orientation;
		Point4DOrientation(int x, int y, int z, int orientation, int dimID)
		{
			this.point= new Point4D(x,y,z,dimID);
			this.orientation=orientation;
		}
		
		int getX()
		{
			return point.getX();
		}
		
		int getY()
		{
			return point.getY();
		}
		
		int getZ()
		{
			return point.getZ();
		}
		
		int getDimension()
		{
			return point.getDimension();
		}
		int getOrientation()
		{
			return orientation;
		}
	}
}

