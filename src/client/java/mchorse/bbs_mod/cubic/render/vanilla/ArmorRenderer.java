package mchorse.bbs_mod.cubic.render.vanilla;

import com.google.common.collect.Maps;
import mchorse.bbs_mod.cubic.model.ArmorType;
import mchorse.bbs_mod.forms.entities.IEntity;
import net.minecraft.client.model.geom.ModelPart;
// [MC 26.2 REMOVED] import net.minecraft.client.renderer.texture.OverlayTexture;
// [MC 26.2 REMOVED] import net.minecraft.client.renderer.rendertype.RenderTypes;
// [MC 26.2 REMOVED] import net.minecraft.client.renderer.Sheets;
import com.mojang.blaze3d.vertex.VertexConsumer;
// [MC 26.2 REMOVED] import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
// [MC26.2] import net.minecraft.client.renderer.texture.SpriteAtlasTexture;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.EquipmentSlot;

// [MC26.2] 
// [MC26.2] 
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
// [MC26.2] import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.resources.Identifier;

import java.util.Map;

public class ArmorRenderer
{
    private static final Map<String, Identifier> ARMOR_TEXTURE_CACHE = Maps.newHashMap();
    private final HumanoidModel innerModel;
    private final HumanoidModel outerModel;
    private final TextureAtlasSpriteAtlasTexture armorTrimsAtlas;

    public ArmorRenderer(HumanoidModel innerModel, HumanoidModel outerModel, ModelManager bakery)
    {
        this.innerModel = innerModel;
        this.outerModel = outerModel;
        this.armorTrimsAtlas = bakery.getAtlas(TexturedRenderTypess.ARMOR_TRIMS_ATLAS_TEXTURE);
    }

    public void renderArmorSlot(PoseStack matrices, VertexConsumer vertexConsumers, IEntity entity, EquipmentSlot armorSlot, ArmorType type, int light)
    {
        ItemStack itemStack = entity.getEquipmentStack(armorSlot);
        Item item = itemStack.getItem();

        if (item instanceof ArmorItem armorItem)
        {
            if (armorItem.getSlotType() == armorSlot)
            {
                boolean innerModel = this.usesInnerModel(armorSlot);
                HumanoidModel bipedModel = this.getModel(armorSlot);
                ModelPart part = this.getPart(bipedModel, type);

                bipedModel.setVisible(true);

                part.pivotX = part.pivotY = part.pivotZ = 0F;
                part.pitch = part.yaw = part.roll = 0F;
                part.xScale = part.yScale = part.zScale = 1F;

                if (armorItem instanceof DyeableLeatherItem dyeableArmorItem)
                {
                    int color = dyeableArmorItem.getColor(itemStack);
                    float r = (float)(color >> 16 & 255) / 255.0F;
                    float g = (float)(color >> 8 & 255) / 255.0F;
                    float b = (float)(color & 255) / 255.0F;

                    this.renderArmorParts(part, matrices, vertexConsumers, light, armorItem, innerModel, r, g, b, null);
                    this.renderArmorParts(part, matrices, vertexConsumers, light, armorItem, innerModel, 1F, 1F, 1F, "overlay");
                }
                else
                {
                    this.renderArmorParts(part, matrices, vertexConsumers, light, armorItem, innerModel, 1F, 1F, 1F, null);
                }

                ArmorTrim.getTrim(entity.getWorld().getRegistryManager(), itemStack, true).ifPresent((trim) ->
                {
                    this.renderTrim(part, armorItem.getMaterial(), matrices, vertexConsumers, light, trim, innerModel);
                });

                if (itemStack.hasGlint())
                {
                    this.renderGlint(part, matrices, vertexConsumers, light);
                }
            }
        }
    }

    private ModelPart getPart(HumanoidModel bipedModel, ArmorType type)
    {
        switch (type)
        {
            case HELMET -> {
                return bipedModel.head;
            }
            case CHEST, LEGGINGS -> {
                return bipedModel.body;
            }
            case LEFT_ARM -> {
                return bipedModel.leftArm;
            }
            case RIGHT_ARM -> {
                return bipedModel.rightArm;
            }
            case LEFT_LEG, LEFT_BOOT -> {
                return bipedModel.leftLeg;
            }
            case RIGHT_LEG, RIGHT_BOOT -> {
                return bipedModel.rightLeg;
            }
        }

        return bipedModel.head;
    }

    private void renderArmorParts(ModelPart part, PoseStack matrices, VertexConsumer vertexConsumers, int light, ArmorItem item, boolean secondTextureLayer, float red, float green, float blue, String overlay)
    {
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderTypes.getArmorCutoutNoCull(this.getArmorTexture(item, secondTextureLayer, overlay)));

        part.render(matrices, vertexConsumer, light, 0, red, green, blue, 1F);
    }

    private void renderTrim(ModelPart part, ArmorMaterials material, PoseStack matrices, VertexConsumer vertexConsumers, int light, ArmorTrim trim, boolean leggings)
    {
        TextureAtlasSprite sprite = this.armorTrimsAtlas.getTextureAtlasSprite(leggings ? trim.getLeggingsModelId(material) : trim.getGenericModelId(material));
        VertexConsumer vertexConsumer = sprite.getTextureSpecificVertexConsumer(vertexConsumers.getBuffer(TexturedRenderTypess.getArmorTrims(trim.getPattern().value().decal())));

        part.render(matrices, vertexConsumer, light, 0, 1F, 1F, 1F, 1F);
    }

    private void renderGlint(ModelPart part, PoseStack matrices, VertexConsumer vertexConsumers, int light)
    {
        part.render(matrices, vertexConsumers.getBuffer(RenderTypes.getArmorEntityGlint()), light, 0, 1F, 1F, 1F, 1F);
    }

    private HumanoidModel getModel(EquipmentSlot slot)
    {
        return this.usesInnerModel(slot) ? this.innerModel : this.outerModel;
    }

    private boolean usesInnerModel(EquipmentSlot slot)
    {
        return slot == EquipmentSlot.LEGS;
    }

    private Identifier getArmorTexture(ArmorItem item, boolean secondLayer, String overlay)
    {
        String materialName = item.getMaterial().getName();
        String id = "textures/models/armor/" + materialName + "_layer_" + (secondLayer ? 2 : 1) + (overlay == null ? "" : "_" + overlay) + ".png";

        return ARMOR_TEXTURE_CACHE.computeIfAbsent(id, Identifier::new);
    }
}


