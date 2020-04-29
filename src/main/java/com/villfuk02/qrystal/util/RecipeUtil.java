package com.villfuk02.qrystal.util;

import com.mojang.datafixers.util.Pair;
import com.villfuk02.qrystal.QrystalConfig;
import com.villfuk02.qrystal.crafting.CustomCuttingRecipe;
import com.villfuk02.qrystal.dataserializers.MaterialManager;
import com.villfuk02.qrystal.init.ModItems;
import com.villfuk02.qrystal.items.CondensedMaterial;
import com.villfuk02.qrystal.items.Crystal;
import com.villfuk02.qrystal.items.CrystalDust;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class RecipeUtil {
    
    public static final int BASE_VALUE = 36;
    
    public enum CuttingType {HAMMER, SAW, LASER}
    
    public static ArrayList<ItemStack> getResult(ArrayList<Pair<ItemStack, Float>> recipe, Random random) {
        ArrayList<ItemStack> result = new ArrayList<>();
        for(Pair<ItemStack, Float> p : recipe) {
            ItemStack is = p.getFirst();
            float baseAmt = p.getSecond();
            int amt = 0;
            if(baseAmt >= 1f) {
                amt = is.getCount() * (int)baseAmt;
                baseAmt -= (int)baseAmt;
            }
            if(baseAmt > 0 && random != null) {
                for(int i = 0; i < is.getCount(); i++) {
                    if(random.nextFloat() < baseAmt)
                        amt++;
                }
            }
            while(amt > is.getMaxStackSize()) {
                amt -= is.getMaxStackSize();
                result.add(getStackWithTag(is.getItem(), is.getMaxStackSize(), is.getTag()));
            }
            if(amt > 0)
                result.add(getStackWithTag(is.getItem(), amt, is.getTag()));
        }
        
        return result;
    }
    
    public static boolean doesCut(ItemStack input, World world, boolean combineDust) {
        if(combineDust && input.getItem() instanceof CrystalDust && input.hasTag() && input.getTag().contains("material", Constants.NBT.TAG_STRING)) {
            int value = ((CrystalDust)input.getItem()).size * input.getCount();
            if(value == ModItems.DUST_SIZES[0] * 64)
                return true;
            for(int l : ModItems.DUST_SIZES) {
                if(l <= value) {
                    return l != ((CrystalDust)input.getItem()).size;
                }
            }
            return false;
        }
        if(input.getItem() instanceof Crystal && input.hasTag() && input.getTag().contains("material", Constants.NBT.TAG_STRING)) {
            if(isQrystalMaterial(input.getTag().getString("material"), false))
                return ((Crystal)input.getItem()).size != CrystalUtil.Size.SEED;
            switch(((Crystal)input.getItem()).size) {
                case SEED:
                    return false;
                case SMALL:
                    return ((Crystal)input.getItem()).tier == 0;
                case MEDIUM:
                case LARGE:
                    return true;
            }
        }
        
        Optional<CustomCuttingRecipe> recipe = world.getRecipeManager().getRecipe(CustomCuttingRecipe.CustomCuttingRecipeType.CUTTING, FakeInventory(input), world);
        if(recipe.isPresent()) {
            return true;
        }
        
        return false;
    }
    
    public static ArrayList<Pair<ItemStack, Float>> getCuttingRecipe(CuttingType cuttingType, int tier, ItemStack input, World world, boolean combineDust) {
        if(combineDust && input.getItem() instanceof CrystalDust && input.hasTag() && input.getTag().contains("material", Constants.NBT.TAG_STRING)) {
            return getDustRecipe(((CrystalDust)input.getItem()).size * input.getCount(), input.getTag().getString("material"), 64, false, 1);
        }
        
        if(input.getItem() instanceof Crystal && input.hasTag() && input.getTag().contains("material", Constants.NBT.TAG_STRING)) {
            ArrayList<Pair<ItemStack, Float>> r = getCrystalCuttingRecipe((Crystal)input.getItem(), input.getTag().getString("material"), cuttingType, tier);
            for(Pair<ItemStack, Float> p : r) {
                p.getFirst().setCount(input.getCount());
            }
            return r;
        }
        
        Optional<CustomCuttingRecipe> recipe = world.getRecipeManager().getRecipe(CustomCuttingRecipe.CustomCuttingRecipeType.CUTTING, FakeInventory(input), world);
        if(recipe.isPresent()) {
            return getCustomCuttingRecipe(cuttingType, input, recipe.get());
        }
        
        return new ArrayList<>();
    }
    
    public static ArrayList<Pair<ItemStack, Float>> getCrystalCuttingRecipe(Crystal input, String mat, CuttingType cuttingType, int tier) {
        ArrayList<Pair<ItemStack, Float>> result = new ArrayList<>();
        if(isQrystalMaterial(mat, false)) {
            switch(input.size) {
                case SEED:
                    return result;
                case SMALL:
                    if(input.tier == 0) {
                        if(QrystalConfig.material_tier_multiplier == 2)
                            result.add(new Pair<>(getStackWithMatTag(ModItems.DUSTS.get("dust_6"), mat), 3f));
                        else if(QrystalConfig.material_tier_multiplier == 3)
                            result.add(new Pair<>(getStackWithMatTag(ModItems.DUSTS.get("dust_6"), mat), 2f));
                        else
                            result.add(new Pair<>(getStackWithMatTag(ModItems.DUSTS.get("dust_1"), mat), 36 / (float)QrystalConfig.material_tier_multiplier));
                    } else {
                        result.add(new Pair<>(getStackWithMatTag(getCrystal(input.tier, CrystalUtil.Size.SEED), mat), (float)seedAmt(cuttingType)));
                    }
                    return result;
                case MEDIUM:
                    result.add(new Pair<>(getStackWithMatTag(getCrystal(input.tier, CrystalUtil.Size.SMALL), mat), (float)(QrystalConfig.material_tier_multiplier * QrystalConfig.qrystal_yield_multiplier)));
                    if(input.tier > 0)
                        result.add(new Pair<>(getStackWithMatTag(getCrystal(input.tier, CrystalUtil.Size.SEED), mat), seedAmt(cuttingType) * 0.05f));
                    return result;
                
                case LARGE:
                    result.add(new Pair<>(getStackWithMatTag(getCrystal(input.tier, CrystalUtil.Size.MEDIUM), mat), (float)(QrystalConfig.material_tier_multiplier * QrystalConfig.qrystal_yield_multiplier)));
                    if(input.tier < 15)
                        result.add(new Pair<>(getStackWithMatTag(getCrystal(input.tier + 1, CrystalUtil.Size.SEED), mat), higherSeedChance(cuttingType, tier, input.tier)));
                    return result;
            }
        }
        if(isQrystalMaterial(mat, true)) {
            switch(input.size) {
                case SEED:
                    return result;
                case SMALL:
                    if(input.tier == 0) {
                        if(QrystalConfig.material_tier_multiplier == 2)
                            result.add(new Pair<>(getStackWithMatTag(ModItems.DUSTS.get("dust_6"), mat), 3f));
                        else if(QrystalConfig.material_tier_multiplier == 3)
                            result.add(new Pair<>(getStackWithMatTag(ModItems.DUSTS.get("dust_6"), mat), 2f));
                        else
                            result.add(new Pair<>(getStackWithMatTag(ModItems.DUSTS.get("dust_1"), mat), 36 / (float)QrystalConfig.material_tier_multiplier));
                    }
                    return result;
                case MEDIUM:
                    result.add(new Pair<>(getStackWithMatTag(getCrystal(input.tier, CrystalUtil.Size.SMALL), mat), (float)(QrystalConfig.material_tier_multiplier * QrystalConfig.qrystal_yield_multiplier)));
                    return result;
                case LARGE:
                    result.add(new Pair<>(getStackWithMatTag(getCrystal(input.tier, CrystalUtil.Size.MEDIUM), mat), (float)(QrystalConfig.material_tier_multiplier * QrystalConfig.qrystal_yield_multiplier)));
                    return result;
            }
        }
        double rawValue = BASE_VALUE;
        switch(input.size) {
            case SEED:
                return result;
            case SMALL:
                if(input.tier == 0) {
                    if(QrystalConfig.material_tier_multiplier == 2)
                        result.add(new Pair<>(getStackWithMatTag(ModItems.DUSTS.get("dust_6"), mat), 3f));
                    else if(QrystalConfig.material_tier_multiplier == 3)
                        result.add(new Pair<>(getStackWithMatTag(ModItems.DUSTS.get("dust_6"), mat), 2f));
                    else
                        result.add(new Pair<>(getStackWithMatTag(ModItems.DUSTS.get("dust_1"), mat), 36 / (float)QrystalConfig.material_tier_multiplier));
                }
                return result;
            case MEDIUM:
                break;
            case LARGE:
                rawValue *= QrystalConfig.material_tier_multiplier;
                break;
        }
        double yieldMulti = QrystalConfig.base_yield_multiplier * Math.pow(QrystalConfig.yield_tier_multiplier, input.tier) * longPositivePower(QrystalConfig.material_tier_multiplier, input.tier);
        double yield = rawValue * yieldMulti * getYieldRate(cuttingType);
        Map<ResourceLocation, Integer> outputs = MaterialManager.materials.get(mat).outputs;
        //BIGGEST
        Pair<ResourceLocation, Integer> biggest = getBiggestOutput(outputs, yield);
        if(biggest.getSecond() > 0) {
            Pair<ItemStack, Double> output = condenseD(new ItemStack(ForgeRegistries.ITEMS.getValue(biggest.getFirst())), mat, yield / biggest.getSecond(), 65);
            int amt = output.getFirst().getCount();
            yield -= biggest.getSecond() * output.getSecond();
            rawValue -= biggest.getSecond() * output.getSecond() / yieldMulti;
            output.getFirst().setCount(1);
            result.add(new Pair<>(output.getFirst(), (float)amt));
            //2ND BIGGEST
            biggest = getBiggestOutput(outputs, yield);
            if(biggest.getSecond() > 0) {
                output = condenseD(new ItemStack(ForgeRegistries.ITEMS.getValue(biggest.getFirst())), mat, yield / biggest.getSecond(), 65);
                amt = output.getFirst().getCount();
                rawValue -= biggest.getSecond() * output.getSecond() / yieldMulti;
                output.getFirst().setCount(1);
                result.add(new Pair<>(output.getFirst(), (float)amt));
            }
        }
        rawValue /= BASE_VALUE;
        if(rawValue > 1)
            result.add(new Pair<>(getStackWithMatTag(getCrystal(input.tier, CrystalUtil.Size.MEDIUM), mat), (float)(int)rawValue));
        rawValue -= (int)rawValue;
        rawValue *= QrystalConfig.material_tier_multiplier;
        if(rawValue > 0)
            result.add(new Pair<>(getStackWithMatTag(getCrystal(input.tier, CrystalUtil.Size.SMALL), mat), (float)rawValue));
        return result;
        
    }
    
    public static long longPositivePower(long base, int power) {
        long res = 1;
        while(true) {
            if((power & 1) == 1)
                res *= base;
            power >>>= 1;
            if(power == 0)
                break;
            base *= base;
        }
        return res;
    }
    
    public static long getCondensedValue(int power) {
        if(power > 10 || power < 0)
            return 0;
        return ((long)1) << (6 * power);
    }
    
    public static ArrayList<Pair<ItemStack, Float>> getDustRecipe(long value, String mat, int stacks, boolean condense, int overshot) {
        ArrayList<Pair<ItemStack, Float>> result = new ArrayList<>();
        
        for(int i = 0; i < stacks; i++) {
            if(value <= 0)
                break;
            for(int l : ModItems.DUST_SIZES) {
                if(l == 1 || l * overshot <= value) {
                    long amt = value / l;
                    if(amt >= 64 && condense) {
                        Pair<ItemStack, Long> output = condenseL(getStackWithMatTag(ModItems.DUSTS.get("dust_" + l), mat), mat, amt, 64);
                        amt = output.getFirst().getCount();
                        output.getFirst().setCount(1);
                        value -= amt * l * output.getSecond();
                        result.add(new Pair<>(output.getFirst(), (float)amt));
                    } else {
                        if(amt > 64)
                            amt = 64;
                        value -= amt * l;
                        result.add(new Pair<>(getStackWithMatTag(ModItems.DUSTS.get("dust_" + l), mat), (float)amt));
                    }
                    break;
                }
            }
            
        }
        return result;
    }
    
    public static ArrayList<Pair<ItemStack, Float>> getCustomCuttingRecipe(CuttingType cuttingType, ItemStack input, CustomCuttingRecipe recipe) {
        ArrayList<Pair<ItemStack, Float>> result = new ArrayList<>();
        for(CustomCuttingRecipe.RecipeOutput o : recipe.getOutputs()) {
            if(cuttingType == CuttingType.HAMMER && o.hammer || cuttingType == CuttingType.SAW && o.saw || cuttingType == CuttingType.LASER && o.laser) {
                result.add(new Pair<>(getStackWithMatTag(o.item, input.getCount(), o.material), o.amt));
            }
        }
        return result;
    }
    
    public static boolean isQrystalMaterial(String mat, boolean qlear) {
        if(mat.equals(CrystalUtil.Color.QLEAR.toString()))
            return qlear;
        for(CrystalUtil.Color c : CrystalUtil.Color.values()) {
            if(mat.equals(c.toString()))
                return true;
        }
        return false;
    }
    
    public static Crystal getCrystal(int tier, CrystalUtil.Size size) {
        return (Crystal)ModItems.CRYSTALS.get(size.toString() + "_" + tier);
    }
    
    public static int seedAmt(CuttingType cuttingType) {
        switch(cuttingType) {
            case HAMMER:
                return 8;
            case SAW:
                return 10;
            case LASER:
                return 12;
        }
        return 0;
    }
    
    public static float higherSeedChance(CuttingType cuttingType, int cuttingTier, int crystalTier) {
        double convertedTier = crystalTier;
        switch(cuttingType) {
            case HAMMER:
                convertedTier -= 2;
                break;
            case SAW:
                convertedTier -= 4;
                break;
            case LASER:
                convertedTier -= 6 + cuttingTier;
                break;
        }
        return (float)Math.min(QrystalConfig.base_seed_chance * Math.pow(0.4, convertedTier), 1);
    }
    
    public static double getYieldRate(CuttingType cuttingType) {
        switch(cuttingType) {
            case HAMMER:
                return 0.8;
            case SAW:
                return 0.9;
            case LASER:
                return 1;
        }
        return 0;
    }
    
    public static Pair<ResourceLocation, Integer> getBiggestOutput(Map<ResourceLocation, Integer> sources, double max) {
        Pair<ResourceLocation, Integer> result = new Pair<>(new ResourceLocation(""), 0);
        for(ResourceLocation k : sources.keySet()) {
            if(sources.get(k) > result.getSecond() && sources.get(k) <= max) {
                result = new Pair<>(k, sources.get(k));
            }
        }
        return result;
    }
    
    public static ItemStack getStackWithTag(IItemProvider item, int count, CompoundNBT tag) {
        if(count <= 0)
            return ItemStack.EMPTY;
        ItemStack r = new ItemStack(item, count);
        r.setTag(tag);
        return r;
    }
    
    public static ItemStack getStackWithTag(IItemProvider item, CompoundNBT tag) {
        return getStackWithTag(item, 1, tag);
    }
    
    public static ItemStack getStackWithMatTag(IItemProvider item, String mat) {
        return getStackWithMatTag(item, 1, mat);
    }
    
    public static ItemStack getStackWithMatTag(IItemProvider item, int count, String mat) {
        CompoundNBT tag = new CompoundNBT();
        if(!mat.isEmpty())
            tag.putString("material", mat);
        return getStackWithTag(item, count, tag);
    }
    
    public static ItemStack getStackWithFluidTag(IItemProvider item, String fluid) {
        CompoundNBT tag = new CompoundNBT();
        tag.putString("fluid", fluid);
        return getStackWithTag(item, tag);
    }
    
    public static Pair<ItemStack, Double> condenseD(ItemStack input, String mat, double amount, int threshold) {
        if(amount >= threshold) {
            amount /= 64;
            double value = 64;
            CompoundNBT tag = new CompoundNBT();
            tag.putString("material", mat);
            tag.putInt("power", 1);
            tag.put("item", input.serializeNBT());
            ItemStack output = getStackWithTag(ModItems.CONDENSED_MATERIAL, 1, tag);
            while(amount >= threshold) {
                amount /= 64;
                value *= 64;
                output.getTag().putInt("power", output.getTag().getInt("power") + 1);
            }
            output.setCount((int)amount);
            return new Pair<>(output, Math.floor(amount) * value);
        } else {
            input.setCount((int)amount);
            return new Pair<>(input, Math.floor(amount));
        }
    }
    
    public static Pair<ItemStack, Long> condenseL(ItemStack input, String mat, long amount, int threshold) {
        if(amount >= threshold) {
            amount /= 64;
            long value = 64;
            CompoundNBT tag = new CompoundNBT();
            tag.putString("material", mat);
            tag.putInt("power", 1);
            tag.put("item", input.serializeNBT());
            ItemStack output = getStackWithTag(ModItems.CONDENSED_MATERIAL, 1, tag);
            while(amount >= threshold) {
                amount /= 64;
                value *= 64;
                output.getTag().putInt("power", output.getTag().getInt("power") + 1);
            }
            output.setCount((int)amount);
            return new Pair<>(output, amount * value);
        } else {
            input.setCount((int)amount);
            return new Pair<>(input, amount);
        }
    }
    
    public static IInventory FakeInventory(ItemStack stack) {
        return new IInventory() {
            @Override
            public int getSizeInventory() {
                return 1;
            }
            
            @Override
            public boolean isEmpty() {
                return false;
            }
            
            @Override
            public ItemStack getStackInSlot(int index) {
                return stack;
            }
            
            @Override
            public ItemStack decrStackSize(int index, int count) {
                return null;
            }
            
            @Override
            public ItemStack removeStackFromSlot(int index) {
                return null;
            }
            
            @Override
            public void setInventorySlotContents(int index, ItemStack stack1) {
            }
            
            @Override
            public void markDirty() {
            }
            
            @Override
            public boolean isUsableByPlayer(PlayerEntity player) {
                return false;
            }
            
            @Override
            public void clear() {
            }
            
            
        };
    }
    
    public static CraftingInventory FakeCraftingInventory(ItemStack stack, int slots) {
        FakeWorkbenchContainer c = new FakeWorkbenchContainer(stack, slots);
        return c.craftMatrix;
    }
    
    public static class FakeWorkbenchContainer extends Container {
        public CraftingInventory craftMatrix = new CraftingInventory(this, 3, 3);
        
        public FakeWorkbenchContainer(ItemStack in, int amt) {
            super(ContainerType.CRAFTING, -999);
            for(int i = 0; i < 3; ++i) {
                for(int j = 0; j < 3; ++j) {
                    addSlot(new Slot(craftMatrix, j + i * 3, 30 + j * 18, 17 + i * 18));
                    if(amt == 2 && j + i * 3 == 2)
                        continue;
                    if(amt > 0)
                        craftMatrix.setInventorySlotContents(j + i * 3, in.copy());
                    amt--;
                }
            }
        }
        
        @Override
        public boolean canInteractWith(PlayerEntity playerIn) {
            return false;
        }
    }
    
    public static Pair<int[], ArrayList<ItemStack>> separateCrystals(String mat, int tier, ItemStack... input) {
        int[] crystals = new int[4];
        ArrayList<ItemStack> result = new ArrayList<>();
        for(ItemStack stack : input) {
            if(!stack.isEmpty()) {
                if(stack.getItem() instanceof Crystal) {
                    Crystal crystal = (Crystal)stack.getItem();
                    if(stack.hasTag() && stack.getTag().contains("material", Constants.NBT.TAG_STRING) && stack.getTag().getString("material").equals(mat)) {
                        if(crystal.tier == tier) {
                            switch(crystal.size) {
                                case SEED:
                                    break;
                                case SMALL:
                                    crystals[0] += stack.getCount();
                                    continue;
                                case MEDIUM:
                                    crystals[1] += stack.getCount();
                                    continue;
                                case LARGE:
                                    crystals[2] += stack.getCount();
                                    continue;
                            }
                        } else if(crystal.tier == tier + 1 && crystal.size == CrystalUtil.Size.SMALL) {
                            crystals[3] += stack.getCount();
                            continue;
                        }
                    }
                }
                result.add(stack);
            }
        }
        return new Pair<>(crystals, result);
    }
    
    
    public static Pair<Integer, ArrayList<ItemStack>> crystallize(String mat, int seeds, int smalls, int tier, ItemStack... input) {
        Pair<int[], ArrayList<ItemStack>> separated = separateCrystals(mat, tier, input);
        int small = separated.getFirst()[0];
        int medium = separated.getFirst()[1];
        int large = separated.getFirst()[2];
        int next = separated.getFirst()[3];
        ArrayList<ItemStack> result = separated.getSecond();
        if(seeds > 0 && smalls >= QrystalConfig.material_tier_multiplier) {
            int amt = Math.min(seeds, smalls / QrystalConfig.material_tier_multiplier);
            smalls -= amt * QrystalConfig.material_tier_multiplier;
            seeds -= amt;
            next += amt;
        }
        if(smalls >= QrystalConfig.material_tier_multiplier * QrystalConfig.material_tier_multiplier) {
            int amt = smalls / (QrystalConfig.material_tier_multiplier * QrystalConfig.material_tier_multiplier);
            smalls -= amt * QrystalConfig.material_tier_multiplier * QrystalConfig.material_tier_multiplier;
            large += amt;
        }
        if(smalls >= QrystalConfig.material_tier_multiplier) {
            int amt = smalls / QrystalConfig.material_tier_multiplier;
            smalls -= amt * QrystalConfig.material_tier_multiplier;
            medium += amt;
        }
        if(smalls >= 1) {
            small += smalls;
        }
        result.addAll(getResult(createCrystals(mat, tier, small, medium, large, next), null));
        return new Pair<>(seeds, result);
    }
    
    private static ArrayList<Pair<ItemStack, Float>> createCrystals(String mat, int tier, int small, int medium, int large, int next) {
        ArrayList<Pair<ItemStack, Float>> result = new ArrayList<>();
        if(small > 0)
            result.add(new Pair<>(getStackWithMatTag(getCrystal(tier, CrystalUtil.Size.SMALL), mat), (float)small));
        if(medium > 0)
            result.add(new Pair<>(getStackWithMatTag(getCrystal(tier, CrystalUtil.Size.MEDIUM), mat), (float)medium));
        if(large > 0)
            result.add(new Pair<>(getStackWithMatTag(getCrystal(tier, CrystalUtil.Size.LARGE), mat), (float)large));
        if(next > 0)
            result.add(new Pair<>(getStackWithMatTag(getCrystal(tier + 1, CrystalUtil.Size.SMALL), mat), (float)next));
        return result;
    }
    
    public static ArrayList<Pair<ItemStack, Float>> roundUp(ArrayList<Pair<ItemStack, Float>> recipe) {
        ArrayList<Pair<ItemStack, Float>> result = new ArrayList<>();
        for(Pair<ItemStack, Float> p : recipe) {
            result.add(new Pair<>(p.getFirst(), (float)MathHelper.ceil(p.getSecond())));
        }
        return result;
    }
    
    public static ArrayList<ItemStack> stackTogether(ItemStack[] oldStacks, ItemStack... newStacks) {
        ArrayList<ItemStack> result = new ArrayList<>();
        for(ItemStack o : oldStacks) {
            result.add(o.copy());
        }
        ArrayList<ItemStack> toStack = new ArrayList<>(Arrays.asList(newStacks));
        while(toStack.size() > 0) {
            ItemStack s = toStack.get(0);
            if(s.isEmpty()) {
                toStack.remove(0);
                continue;
            }
            boolean found = false;
            for(int i = 0; i < result.size(); i++) {
                ItemStack r = result.get(i);
                if(r.isEmpty()) {
                    result.set(i, s.copy());
                    toStack.remove(0);
                    found = true;
                    break;
                }
                if(ItemHandlerHelper.canItemStacksStack(s, r)) {
                    int amt = s.getCount() + r.getCount();
                    if(amt <= s.getMaxStackSize()) {
                        r.setCount(amt);
                        toStack.remove(0);
                        found = true;
                        break;
                    } else {
                        amt -= s.getMaxStackSize();
                        r.setCount(s.getMaxStackSize());
                        s.setCount(amt);
                    }
                }
            }
            if(!found)
                result.add(s);
        }
        return result;
    }
    
    public static void forceInsertSameOrEmptyStack(ItemStackHandler inventory, int i, ItemStack stack) {
        ItemStack result = inventory.getStackInSlot(i);
        if(result.isEmpty()) {
            inventory.setStackInSlot(i, stack.copy());
        } else {
            result.setCount(result.getCount() + stack.getCount());
            inventory.setStackInSlot(i, result);
        }
    }
    
    public static String getAssociatedMaterial(ItemStack item) {
        String mat = "";
        if(item.hasTag() && item.getTag().contains("material", Constants.NBT.TAG_STRING)) {
            mat = item.getTag().getString("material");
        } else {
            for(String m : MaterialManager.materials.keySet()) {
                if(MaterialManager.materials.get(m).outputs.keySet().contains(item.getItem().getRegistryName())) {
                    mat = m;
                    break;
                }
            }
            if(mat.isEmpty()) {
                if(MaterialManager.dissolvable.containsKey(item.getItem().getRegistryName()))
                    mat = MaterialManager.dissolvable.get(item.getItem().getRegistryName()).material;
            }
        }
        return mat;
    }
    
    public static int hashPos(BlockPos p) {
        int i = p.getX() * p.getZ();
        i += 23;
        i *= p.getY();
        i = i ^ p.getZ();
        i = (i * 7) ^ p.getX();
        return (i * 3 + 41) ^ p.getY() ^ p.getZ();
    }
    
    public static Pair<ItemStack, Integer>[] getComponentList(ItemStack in, World w) {
        if(in.getItem() instanceof CondensedMaterial) {
            if(in.hasTag() && in.getTag().contains("power", Constants.NBT.TAG_INT) && in.getTag().contains("item", Constants.NBT.TAG_COMPOUND))
                in = ItemStack.read(in.getTag().getCompound("item"));
            else
                return new Pair[0];
        }
        ArrayList<Pair<ItemStack, Pair<Byte, Byte>>> open = new ArrayList<>();
        ArrayList<Pair<ItemStack, Pair<Byte, Byte>>> closed = new ArrayList<>();
        open.add(new Pair<>(in, new Pair<>((byte)0, (byte)0)));
        while(open.size() > 0) {
            Pair<ItemStack, Pair<Byte, Byte>> current = open.get(0);
            closed.add(new Pair<>(current.getFirst(), current.getSecond()));
            open.remove(0);
            ItemStack temp = upstack(current.getFirst(), 9, w);
            if(!temp.isEmpty())
                open.add(new Pair<>(temp, new Pair<>(current.getSecond().getFirst(), (byte)(current.getSecond().getSecond() + (byte)2))));
            temp = upstack(current.getFirst(), 6, w);
            if(!temp.isEmpty())
                open.add(new Pair<>(temp, new Pair<>((byte)(current.getSecond().getFirst() + (byte)1), (byte)(current.getSecond().getSecond() + (byte)1))));
            temp = upstack(current.getFirst(), 4, w);
            if(!temp.isEmpty())
                open.add(new Pair<>(temp, new Pair<>((byte)(current.getSecond().getFirst() + (byte)2), current.getSecond().getSecond())));
        }
        Pair<ItemStack, Integer> base = new Pair<>(ItemStack.EMPTY, 0);
        for(int i = 0; i < closed.size(); i++) {
            long power = longPositivePower(3, closed.get(i).getSecond().getSecond().intValue()) << closed.get(i).getSecond().getFirst().intValue();
            if(power > Integer.MAX_VALUE) {
                base = new Pair<>(in, 1);
                break;
            }
            if(power > base.getSecond())
                base = new Pair<>(closed.get(i).getFirst(), (int)power);
        }
        open.clear();
        closed.clear();
        open.add(new Pair<>(base.getFirst(), new Pair<>((byte)0, (byte)0)));
        byte max2 = 0;
        byte max3 = 0;
        long max = 1;
        while(open.size() > 0) {
            Pair<ItemStack, Pair<Byte, Byte>> current = open.get(0);
            closed.add(new Pair<>(current.getFirst(), current.getSecond()));
            open.remove(0);
            Pair<ItemStack, Integer> temp = downstack(current.getFirst(), w);
            max = longPositivePower(3, max3) << (int)(max2);
            if(temp != null) {
                if(temp.getSecond() == 9 && max * 9 < Integer.MAX_VALUE) {
                    open.add(new Pair<>(temp.getFirst(), new Pair<>(current.getSecond().getFirst(), (byte)(current.getSecond().getSecond() + (byte)2))));
                    if(current.getSecond().getSecond() + (byte)2 > max3)
                        max3 = (byte)(current.getSecond().getSecond() + (byte)2);
                } else if(temp.getSecond() == 6 && max * 6 < Integer.MAX_VALUE) {
                    open.add(new Pair<>(temp.getFirst(), new Pair<>((byte)(current.getSecond().getFirst() + (byte)1), (byte)(current.getSecond().getSecond() + (byte)1))));
                    if(current.getSecond().getSecond() + (byte)1 > max3)
                        max3 = (byte)(current.getSecond().getSecond() + (byte)1);
                    if(current.getSecond().getFirst() + (byte)1 > max2)
                        max2 = (byte)(current.getSecond().getFirst() + (byte)1);
                } else if(temp.getSecond() == 4 && max * 4 < Integer.MAX_VALUE) {
                    open.add(new Pair<>(temp.getFirst(), new Pair<>((byte)(current.getSecond().getFirst() + (byte)2), current.getSecond().getSecond())));
                    if(current.getSecond().getFirst() + (byte)2 > max2)
                        max2 = (byte)(current.getSecond().getFirst() + (byte)2);
                }
            }
        }
        ArrayList<Pair<ItemStack, Integer>> map = new ArrayList<>();
        for(int i = 0; i < closed.size(); i++) {
            map.add(new Pair<>(closed.get(i).getFirst(), (int)(max / (longPositivePower(3, closed.get(i).getSecond().getSecond().intValue()) << closed.get(i).getSecond().getFirst().intValue()))));
        }
        return map.toArray(new Pair[0]);
    }
    
    public static ItemStack upstack(ItemStack in, int amt, World w) {
        CraftingInventory c = FakeCraftingInventory(in, amt);
        Optional<ICraftingRecipe> o = w.getRecipeManager().getRecipe(IRecipeType.CRAFTING, c, w);
        if(!o.isPresent())
            return ItemStack.EMPTY;
        ItemStack r = o.get().getCraftingResult(c);
        if(r.isEmpty() || r.getCount() != 1)
            return ItemStack.EMPTY;
        c = FakeCraftingInventory(r, 1);
        o = w.getRecipeManager().getRecipe(IRecipeType.CRAFTING, c, w);
        if(!o.isPresent())
            return ItemStack.EMPTY;
        ItemStack s = o.get().getCraftingResult(c);
        if(s.isEmpty() || s.getCount() != amt)
            return ItemStack.EMPTY;
        if(ItemHandlerHelper.canItemStacksStack(s, in))
            return r;
        return ItemStack.EMPTY;
    }
    
    public static Pair<ItemStack, Integer> downstack(ItemStack in, World w) {
        CraftingInventory c = FakeCraftingInventory(in, 1);
        Optional<ICraftingRecipe> o = w.getRecipeManager().getRecipe(IRecipeType.CRAFTING, c, w);
        if(!o.isPresent())
            return null;
        ItemStack r = o.get().getCraftingResult(c);
        if(r.isEmpty() || r.getCount() <= 4)
            return null;
        ItemStack n = r.copy();
        n.setCount(1);
        c = FakeCraftingInventory(n, r.getCount());
        o = w.getRecipeManager().getRecipe(IRecipeType.CRAFTING, c, w);
        if(!o.isPresent())
            return null;
        ItemStack s = o.get().getCraftingResult(c);
        if(s.isEmpty() || s.getCount() != 1)
            return null;
        if(ItemHandlerHelper.canItemStacksStack(s, in))
            return new Pair<>(n, r.getCount());
        return null;
    }
    
}
