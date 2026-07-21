package mchorse.bbs_mod.cubic.render.vanilla;

import com.google.common.collect.Maps;
import mchorse.bbs_mod.cubic.model.ArmorType;
import mchorse.bbs_mod.forms.entities.IEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;

public class ArmorRenderer
{
    private static final Map<String, Identifier> ARMOR_TEXTURE_CACHE = Maps.newHashMap();

    private final HumanoidModel innerModel;
    private final HumanoidModel outerModel;
    private boolean slim;

    public ArmorRenderer(ModelManager manager, boolean slim)
    {
        this.innerModel = new HumanoidModel(innerModelPart());
        this.outerModel = new HumanoidModel(outerModelPart());
        this.slim = slim;
    }

    private ModelPart innerModelPart() { return new ModelPart(List.of(), Map.of()); }
    private ModelPart outerModelPart() { return new ModelPart(List.of(), Map.of()); }

    public void renderArmorSlot(PoseStack matrices, VertexConsumer vertexConsumers, IEntity entity, EquipmentSlot armorSlot, ArmorType type, int light)
    {
        renderArmorParts(getModel(armorSlot).root(), matrices, vertexConsumers, light, armorSlot, type, 1F, 1F, 1F);
    }

    private void renderArmorParts(ModelPart part, PoseStack matrices, VertexConsumer vertexConsumers, int light, EquipmentSlot slot, ArmorType type, float red, float green, float blue)
    {
        int color = 0xFF000000 | ((int) (red * 255) << 16) | ((int) (green * 255) << 8) | (int) (blue * 255);
        part.render(matrices, vertexConsumers, light, 0, color);
    }

    private HumanoidModel getModel(EquipmentSlot slot)
    {
        return slot == EquipmentSlot.LEGS ? this.innerModel : this.outerModel;
    }

    private boolean usesInnerModel(EquipmentSlot slot)
    {
        return slot == EquipmentSlot.LEGS;
    }

    private Identifier getArmorTexture(EquipmentSlot slot, boolean secondLayer, String overlay)
    {
        String materialName = slot.getName();
        String id = "textures/models/armor/" + materialName + "_layer_" + (secondLayer ? 2 : 1) + (overlay == null ? "" : "_" + overlay) + ".png";
        return ARMOR_TEXTURE_CACHE.computeIfAbsent(id, Identifier::parse);
    }
}
