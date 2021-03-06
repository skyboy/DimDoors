package StevenDimDoors.mod_pocketDim;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;
import StevenDimDoors.mod_pocketDim.util.WeightedContainer;

/*
 * Registers a category of loot chests for Dimensional Doors in Forge.
 */
public class DDLoot {
	
	private static final double MIN_ITEM_DAMAGE = 0.3;
	private static final double MAX_ITEM_DAMAGE = 0.9;
	private static final int ITEM_ENCHANTMENT_CHANCE = 50;
	private static final int MAX_ITEM_ENCHANTMENT_CHANCE = 100;
	
	public static final String DIMENSIONAL_DUNGEON_CHEST = "dimensionalDungeonChest";
	public static ChestGenHooks DungeonChestInfo = null;
	private static final int CHEST_SIZE = 5;
	
	private DDLoot() { }
	
	public static void registerInfo(DDProperties properties)
	{
		// Register the dimensional dungeon chest with ChestGenHooks. This isn't necessary, but allows
		// other mods to add their own loot to our chests if they know our loot category, without having
		// to interface with our code.
		DungeonChestInfo = ChestGenHooks.getInfo(DIMENSIONAL_DUNGEON_CHEST);
		DungeonChestInfo.setMin(CHEST_SIZE);
		DungeonChestInfo.setMax(CHEST_SIZE);
		
		ArrayList<WeightedRandomChestContent> items = new ArrayList<WeightedRandomChestContent>();
		
		addContent(true, items, Item.ingotIron.itemID, 160, 1, 3);
		addContent(true, items, Item.coal.itemID, 120, 1, 3);
		addContent(true, items, Item.netherQuartz.itemID, 120, 1, 3);
		addContent(true, items, Item.enchantedBook.itemID, 100);
		addContent(true, items, Item.ingotGold.itemID, 80, 1, 3);
		addContent(true, items, Item.diamond.itemID, 40, 1, 2);
		addContent(true, items, Item.emerald.itemID, 20, 1, 2);
		addContent(true, items, Item.appleGold.itemID, 10);

		addContent(properties.FabricOfRealityLootEnabled, items, mod_pocketDim.blockDimWall.blockID, 80, 4, 16);
		addContent(properties.WorldThreadLootEnabled, items, mod_pocketDim.itemWorldThread.itemID, 80);
		
		// Add all the items to our dungeon chest
		addItemsToContainer(DungeonChestInfo, items);
	}
		
	private static void addContent(boolean include, ArrayList<WeightedRandomChestContent> items,
			int itemID, int weight)
	{
		if (include)
			items.add(new WeightedRandomChestContent(itemID, 0, 1, 1, weight));
	}
	
	private static void addContent(boolean include, ArrayList<WeightedRandomChestContent> items,
			int itemID, int weight, int minAmount, int maxAmount)
	{
		if (include)
			items.add(new WeightedRandomChestContent(itemID, 0, minAmount, maxAmount, weight));
	}
	
	private static void addItemsToContainer(ChestGenHooks container, ArrayList<WeightedRandomChestContent> items)
	{
		for (WeightedRandomChestContent item : items)
		{
			container.addItem(item);
		}
	}
	
	private static void fillChest(ArrayList<ItemStack> stacks, IInventory inventory, Random random)
	{
		// This custom chest-filling function avoids overwriting item stacks
		
		// The prime number below is used for choosing chest slots in a seemingly-random pattern. Its value
		// was selected specifically to achieve a spread-out distribution for chests with up to 104 slots.
		// Choosing a prime number ensures that our increments are relatively-prime to the chest size, which
		// means we'll cover all the slots before repeating any. This is mathematically guaranteed.
		final int primeOffset = 239333;

		int size = inventory.getSizeInventory();
		for (ItemStack item : stacks)
        {
        	int limit = size;
        	int index = random.nextInt(size);

        	while (limit > 0 && inventory.getStackInSlot(index) != null)
        	{
        		limit--;
        		index = (index + primeOffset) % size;
        	}
        	
            inventory.setInventorySlotContents(index, item);
        }
	}
	
	public static void generateChestContents(ChestGenHooks chestInfo, IInventory inventory, Random random)
    {
		// This is a custom version of net.minecraft.util.WeightedRandomChestContent.generateChestContents()
		// It's designed to avoid the following bugs in MC 1.5:
		// 1. If multiple enchanted books appear, then they will have the same enchantment
		// 2. The randomized filling algorithm will sometimes overwrite item stacks with other stacks
		
		int count = chestInfo.getCount(random);
		WeightedRandomChestContent[] content = chestInfo.getItems(random);
		ArrayList<ItemStack> allStacks = new ArrayList<ItemStack>();
		
        for (int k = 0; k < count; k++)
        {
            WeightedRandomChestContent selection = (WeightedRandomChestContent)WeightedRandom.getRandomItem(random, content);
            
            // Call getChestGenBase() to make sure we generate a different enchantment for books.
            // Don't just use a condition to check if the item is an instance of ItemEnchantedBook because
            // we don't know if other mods might add items that also need to be regenerated.
            selection = selection.theItemId.getItem().getChestGenBase(chestInfo, random, selection);
            
            ItemStack[] stacks = ChestGenHooks.generateStacks(random, selection.theItemId, selection.theMinimumChanceToGenerateItem, selection.theMaximumChanceToGenerateItem);
            for (int h = 0; h < stacks.length; h++)
            {
            	allStacks.add(stacks[h]);
            }
        }
        
        fillChest(allStacks, inventory, random);
    }
	
	public static void fillGraveChest(IInventory inventory, Random random, DDProperties properties)
	{
		// This function fills "grave chests", which are chests for dungeons that
		// look like a player died in the area and his remains were gathered in
		// a chest. Doing this properly requires fine control of loot generation,
		// so we use our own function rather than Minecraft's functions.
		int k;
		int count;
		ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
		ArrayList<WeightedContainer<Item>> selection = new ArrayList<WeightedContainer<Item>>();
		
		// Insert bones and rotten flesh
		// Make stacks of single items to spread them out
		count = MathHelper.getRandomIntegerInRange(random, 2, 5);
		for (k = 0; k < count; k++)
		{
			stacks.add( new ItemStack(Item.bone, 1) );
		}
		count = MathHelper.getRandomIntegerInRange(random, 2, 4);
		for (k = 0; k < count; k++)
		{
			stacks.add( new ItemStack(Item.rottenFlesh, 1) );
		}
		
		// Insert tools
		// 30% chance of adding a pickaxe
		if (random.nextInt(100) < 30)
		{
			addModifiedTool(Item.pickaxeIron, stacks, random);
		}
		// 30% chance of adding a bow and some arrows
		if (random.nextInt(100) < 30)
		{
			addModifiedBow(stacks, random);
			stacks.add( new ItemStack(Item.arrow, MathHelper.getRandomIntegerInRange(random, 8, 32)) );
		}
		// 10% chance of adding a Rift Blade (no enchants)
		if (properties.RiftBladeLootEnabled && random.nextInt(100) < 10)
		{
			stacks.add( new ItemStack(mod_pocketDim.itemRiftBlade, 1) );
		}
		else
		{
			// 20% of adding an iron sword, 10% of adding a stone sword
			addModifiedSword( getRandomItem(Item.swordIron, Item.swordStone, null, 20, 10, random) , stacks, random);
		}
		
		// Insert equipment
		// For each piece, 25% of an iron piece, 10% of a chainmail piece
		addModifiedEquipment( getRandomItem(Item.helmetIron, Item.helmetChain, null, 25, 10, random) , stacks, random);
		addModifiedEquipment( getRandomItem(Item.plateIron, Item.plateChain, null, 25, 10, random) , stacks, random);
		addModifiedEquipment( getRandomItem(Item.legsIron, Item.legsChain, null, 25, 10, random) , stacks, random);
		addModifiedEquipment( getRandomItem(Item.bootsIron, Item.bootsChain, null, 25, 10, random) , stacks, random);
		
		// Insert other random stuff
		// 40% chance for a name tag, 35% chance for a glass bottle
		// 30% chance for an ender pearl, 5% chance for record 11
		addItemWithChance(stacks, random, 40, Item.nameTag, 1);
		addItemWithChance(stacks, random, 35, Item.glassBottle, 1);
		addItemWithChance(stacks, random, 30, Item.enderPearl, 1);
		addItemWithChance(stacks, random, 5, Item.record11, 1);
		
		// Finally, there is a 3% chance of adding a player head
		if (random.nextInt(100) < 50) // FIXME: SET TO 50% FOR TESTING, CHANGE TO 3%
		{
			stacks.add( new ItemStack(Block.skull) );
		}
		
		fillChest(stacks, inventory, random);
	}
	
	private static void addModifiedEquipment(Item item, ArrayList<ItemStack> stacks, Random random)
	{
		if (item == null)
			return;
		
		stacks.add( getModifiedItem(item, random, new Enchantment[] { Enchantment.blastProtection, Enchantment.fireProtection, Enchantment.protection, Enchantment.projectileProtection }) );
	}

	private static void addModifiedSword(Item item, ArrayList<ItemStack> stacks, Random random)
	{
		if (item == null)
			return;
		
		stacks.add( getModifiedItem(item, random, new Enchantment[] { Enchantment.fireAspect, Enchantment.knockback, Enchantment.sharpness }) );
	}

	private static void addModifiedTool(Item tool, ArrayList<ItemStack> stacks, Random random)
	{
		if (tool == null)
			return;
		
		stacks.add( getModifiedItem(tool, random, new Enchantment[] { Enchantment.efficiency, Enchantment.unbreaking }) );
	}
	
	private static void addModifiedBow(ArrayList<ItemStack> stacks, Random random)
	{
		stacks.add( getModifiedItem(Item.bow, random, new Enchantment[] { Enchantment.flame, Enchantment.power, Enchantment.punch }) );
	}
	
	private static ItemStack getModifiedItem(Item item, Random random, Enchantment[] enchantments)
	{
		ItemStack result = applyRandomDamage(item, random);
		if (enchantments.length > 0 && random.nextInt(MAX_ITEM_ENCHANTMENT_CHANCE) < ITEM_ENCHANTMENT_CHANCE)
		{
			result.addEnchantment(enchantments[ random.nextInt(enchantments.length) ], 1);
		}
		return result;
	}
	
	private static Item getRandomItem(Item a, Item b, Item c, int weightA, int weightB, Random random)
	{
		int roll = random.nextInt(100);
		if (roll < weightA)
			return a;
		if (roll < weightA + weightB)
			return b;
		return c;
	}

	private static void addItemWithChance(ArrayList<ItemStack> stacks, Random random, int chance, Item item, int count)
	{
		if (random.nextInt(100) < chance)
		{
			stacks.add(new ItemStack(item, count));
		}
	}
	
	private static ItemStack applyRandomDamage(Item item, Random random)
	{
		int damage = (int) (item.getMaxDamage() * MathHelper.getRandomDoubleInRange(random, MIN_ITEM_DAMAGE, MAX_ITEM_DAMAGE));
		return new ItemStack(item, 1, damage);
	}
}
