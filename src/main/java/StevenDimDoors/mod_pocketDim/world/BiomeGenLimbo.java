package StevenDimDoors.mod_pocketDim.world;

import net.minecraft.world.biome.BiomeGenBase;

public class BiomeGenLimbo extends BiomeGenBase
{
	public BiomeGenLimbo(int par1)
    {
        super(par1);
        this.theBiomeDecorator.treesPerChunk = 0;
        this.theBiomeDecorator.flowersPerChunk = 0;
        this.theBiomeDecorator.grassPerChunk = 0;
        this.setBiomeName("Limbo");
        this.setDisableRain();
        
        this.spawnableMonsterList.clear();
        this.spawnableCreatureList.clear();
        this.spawnableWaterCreatureList.clear();
        this.spawnableCaveCreatureList.clear();
      //  this.spawnableMonsterList.add(new SpawnListEntry(MobObelisk.class, 1, 1, 1));
       // this.spawnableMonsterList.add(new SpawnListEntry(MobObelisk.class, 300, 0, 0));

      //  this.spawnableCreatureList.add(new SpawnListEntry(MobObelisk.class, 1, 1, 1));
      //  this.spawnableCreatureList.add(new SpawnListEntry(MobObelisk.class, 300, 0, 0));

      //  this.spawnableCaveCreatureList.add(new SpawnListEntry(MobObelisk.class, 1, 1, 1));
     //   this.spawnableCaveCreatureList.add(new SpawnListEntry(MobObelisk.class, 300, 0, 0));




    }
    
    @Override
	public float getSpawningChance()
    {
        return 0.00001F;
    }
}
