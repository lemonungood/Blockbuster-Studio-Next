package mchorse.bbs_mod.network;

import mchorse.bbs_mod.BBSModClient;
import mchorse.bbs_mod.actions.ActionState;
import mchorse.bbs_mod.blocks.entities.ModelBlockEntity;
import mchorse.bbs_mod.blocks.entities.ModelProperties;
import mchorse.bbs_mod.client.BBSRendering;
import mchorse.bbs_mod.data.DataStorageUtils;
import mchorse.bbs_mod.data.types.BaseType;
import mchorse.bbs_mod.data.types.MapType;
import mchorse.bbs_mod.entity.GunProjectileEntity;
import mchorse.bbs_mod.entity.IEntityFormProvider;
import mchorse.bbs_mod.film.Film;
import mchorse.bbs_mod.film.Films;
import mchorse.bbs_mod.forms.FormUtils;
import mchorse.bbs_mod.forms.forms.Form;
import mchorse.bbs_mod.items.GunProperties;
import mchorse.bbs_mod.morphing.Morph;
import mchorse.bbs_mod.settings.values.base.BaseValue;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.dashboard.UIDashboard;
import mchorse.bbs_mod.ui.film.UIFilmPanel;
import mchorse.bbs_mod.ui.framework.UIBaseMenu;
import mchorse.bbs_mod.ui.framework.UIScreen;
import mchorse.bbs_mod.ui.model_blocks.UIModelBlockPanel;
import mchorse.bbs_mod.ui.morphing.UIMorphingPanel;
import mchorse.bbs_mod.utils.DataPath;
import mchorse.bbs_mod.utils.repos.RepositoryOperation;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class ClientNetwork
{
    private static int ids = 0;
    private static Map<Integer, Consumer<BaseType>> callbacks = new HashMap<>();
    private static ClientPacketCrusher crusher = new ClientPacketCrusher();

    private static boolean isBBSModOnServer;

    public static void resetInteractionHandshake()
    {
        isBBSModOnServer = false;
        crusher.reset();
    }

    public static boolean isIsBBSModOnServer()
    {
        return isBBSModOnServer;
    }

    /* Network */

    public static void setup()
    {
        ClientPlayNetworking.registerGlobalReceiver(ServerNetwork.CLIENT_CLICKED_MODEL_BLOCK_PACKET, (client, handler, buf, responseSender) -> handleClientModelBlockPacket(client, buf));
        ClientPlayNetworking.registerGlobalReceiver(ServerNetwork.CLIENT_PLAYER_FORM_PACKET, (client, handler, buf, responseSender) -> handlePlayerFormPacket(client, buf));
        ClientPlayNetworking.registerGlobalReceiver(ServerNetwork.CLIENT_PLAY_FILM_PACKET, (client, handler, buf, responseSender) -> handlePlayFilmPacket(client, buf));
        ClientPlayNetworking.registerGlobalReceiver(ServerNetwork.CLIENT_MANAGER_DATA_PACKET, (client, handler, buf, responseSender) -> handleManagerDataPacket(client, buf));
        ClientPlayNetworking.registerGlobalReceiver(ServerNetwork.CLIENT_STOP_FILM_PACKET, (client, handler, buf, responseSender) -> handleStopFilmPacket(client, buf));
        ClientPlayNetworking.registerGlobalReceiver(ServerNetwork.CLIENT_HANDSHAKE, (client, handler, buf, responseSender) -> handleInteractionHandshakePacket(client, buf));
        ClientPlayNetworking.registerGlobalReceiver(ServerNetwork.CLIENT_RECORDED_ACTIONS, (client, handler, buf, responseSender) -> handleRecordedActionsPacket(client, buf));
        ClientPlayNetworking.registerGlobalReceiver(ServerNetwork.CLIENT_ANIMATION_STATE_TRIGGER, (client, handler, buf, responseSender) -> handleFormTriggerPacket(client, buf));
        ClientPlayNetworking.registerGlobalReceiver(ServerNetwork.CLIENT_CHEATS_PERMISSION, (client, handler, buf, responseSender) -> handleCheatsPermissionPacket(client, buf));
        ClientPlayNetworking.registerGlobalReceiver(ServerNetwork.CLIENT_SHARED_FORM, (client, handler, buf, responseSender) -> handleShareFormPacket(client, buf));
        ClientPlayNetworking.registerGlobalReceiver(ServerNetwork.CLIENT_ENTITY_FORM, (client, handler, buf, responseSender) -> handleEntityFormPacket(client, buf));
        ClientPlayNetworking.registerGlobalReceiver(ServerNetwork.CLIENT_ACTORS, (client, handler, buf, responseSender) -> handleActorsPacket(client, buf));
        ClientPlayNetworking.registerGlobalReceiver(ServerNetwork.CLIENT_GUN_PROPERTIES, (client, handler, buf, responseSender) -> handleGunPropertiesPacket(client, buf));
        ClientPlayNetworking.registerGlobalReceiver(ServerNetwork.CLIENT_PAUSE_FILM, (client, handler, buf, responseSender) -> handlePauseFilmPacket(client, buf));
        ClientPlayNetworking.registerGlobalReceiver(ServerNetwork.CLIENT_SELECTED_SLOT, (client, handler, buf, responseSender) -> handleSelectedSlotPacket(client, buf));
        ClientPlayNetworking.registerGlobalReceiver(ServerNetwork.CLIENT_ANIMATION_STATE_MODEL_BLOCK_TRIGGER, (client, handler, buf, responseSender) -> handleAnimationStateModelBlockPacket(client, buf));
        ClientPlayNetworking.registerGlobalReceiver(ServerNetwork.CLIENT_REFRESH_MODEL_BLOCKS, (client, handler, buf, responseSender) -> handleRefreshModelBlocksPacket(client, buf));
    }

    /* InteractionHandlers */

    private static void handleClientModelBlockPacket(Minecraft client, FriendlyByteBuf buf)
    {
        BlockPos pos = buf.readBlockPos();

        client.execute(() ->
        {
            BlockEntity entity = client.world.getBlockEntity(pos);

            if (!(entity instanceof ModelBlockEntity))
            {
                return;
            }

            UIBaseMenu menu = UIScreen.getCurrentMenu();
            UIDashboard dashboard = BBSModClient.getDashboard();

            if (menu != dashboard)
            {
                UIScreen.open(dashboard);
            }

            UIModelBlockPanel panel = dashboard.getPanels().getPanel(UIModelBlockPanel.class);

            dashboard.setPanel(panel);
            panel.fill((ModelBlockEntity) entity, true);
        });
    }

    private static void handlePlayerFormPacket(Minecraft client, FriendlyByteBuf buf)
    {
        crusher.receive(buf, (bytes, packetByteBuf) ->
        {
            int id = packetByteBuf.readInt();
            Form form = FormUtils.fromData(DataStorageUtils.readFromBytes(bytes));

            final Form finalForm = form;

            client.execute(() ->
            {
                Entity entity = client.world.getEntityById(id);
                Morph morph = Morph.getMorph(entity);

                if (morph != null)
                {
                    morph.setForm(finalForm);
                }
            });
        });
    }

    private static void handlePlayFilmPacket(Minecraft client, FriendlyByteBuf buf)
    {
        crusher.receive(buf, (bytes, packetByteBuf) ->
        {
            String filmId = packetByteBuf.readString();
            boolean withCamera = packetByteBuf.readBoolean();
            Film film = new Film();

            film.setId(filmId);
            film.fromData(DataStorageUtils.readFromBytes(bytes));

            client.execute(() -> Films.playFilm(film, withCamera));
        });
    }

    private static void handleManagerDataPacket(Minecraft client, FriendlyByteBuf buf)
    {
        crusher.receive(buf, (bytes, packetByteBuf) ->
        {
            int callbackId = packetByteBuf.readInt();
            RepositoryOperation op = RepositoryOperation.values()[packetByteBuf.readInt()];
            BaseType data = DataStorageUtils.readFromBytes(bytes);

            client.execute(() ->
            {
                Consumer<BaseType> callback = callbacks.remove(callbackId);

                if (callback != null)
                {
                    callback.accept(data);
                }
            });
        });
    }

    private static void handleStopFilmPacket(Minecraft client, FriendlyByteBuf buf)
    {
        String filmId = buf.readString();

        client.execute(() -> Films.stopFilm(filmId));
    }

    private static void handleInteractionHandshakePacket(Minecraft client, FriendlyByteBuf buf)
    {
        isBBSModOnServer = true;
    }

    private static void handleRecordedActionsPacket(Minecraft client, FriendlyByteBuf buf)
    {
        crusher.receive(buf, (bytes, packetByteBuf) ->
        {
            String filmId = packetByteBuf.readString();
            int replayId = packetByteBuf.readInt();
            int tick = packetByteBuf.readInt();
            BaseType data = DataStorageUtils.readFromBytes(bytes);

            client.execute(() ->
            {
                BBSModClient.getDashboard().getPanels().getPanel(UIFilmPanel.class).receiveActions(filmId, replayId, tick, data);
            });
        });
    }

    private static void handleFormTriggerPacket(Minecraft client, FriendlyByteBuf buf)
    {
        int id = buf.readInt();
        String triggerId = buf.readString();
        int type = buf.readInt();

        client.execute(() ->
        {
            Entity entity = client.world.getEntityById(id);
            Morph morph = Morph.getMorph(entity);

            if (morph != null && morph.getForm() != null)
            {
                morph.getForm().playState(triggerId);
            }

            if (entity instanceof LivingEntity livingEntity && type > 0)
            {
                ItemStack stackInInteractionHand = livingEntity.getStackInInteractionHand(type == 1 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
                ModelProperties properties = BBSModClient.getItemStackProperties(stackInInteractionHand);

                if (properties != null && properties.getForm() != null)
                {
                    properties.getForm().playState(triggerId);
                }
            }
        });
    }

    private static void handleCheatsPermissionPacket(Minecraft client, FriendlyByteBuf buf)
    {
        boolean cheats = buf.readBoolean();

        client.execute(() ->
        {
            client.player.setClientPermissionLevel(cheats ? 4 : 0);
        });
    }

    private static void handleShareFormPacket(Minecraft client, FriendlyByteBuf buf)
    {
        crusher.receive(buf, (bytes, packetByteBuf) ->
        {
            final Form finalForm = FormUtils.fromData(DataStorageUtils.readFromBytes(bytes));

            if (finalForm == null)
            {
                return;
            }

            client.execute(() ->
            {
                UIBaseMenu menu = UIScreen.getCurrentMenu();
                UIDashboard dashboard = BBSModClient.getDashboard();

                if (menu == null)
                {
                    UIScreen.open(dashboard);
                }

                dashboard.setPanel(dashboard.getPanel(UIMorphingPanel.class));
                BBSModClient.getFormCategories().getRecentForms().getCategories().get(0).addForm(finalForm);
                dashboard.context.notifyInfo(UIKeys.FORMS_SHARED_NOTIFICATION.format(finalForm.getDisplayName()));
            });
        });
    }

    private static void handleEntityFormPacket(Minecraft client, FriendlyByteBuf buf)
    {
        crusher.receive(buf, (bytes, packetByteBuf) ->
        {
            final Form finalForm = FormUtils.fromData(DataStorageUtils.readFromBytes(bytes));

            if (finalForm == null)
            {
                return;
            }

            int entityId = buf.readInt();

            client.execute(() ->
            {
                Entity entity = client.world.getEntityById(entityId);

                if (entity instanceof IEntityFormProvider provider)
                {
                    provider.setForm(finalForm);
                }
            });
        });
    }

    private static void handleActorsPacket(Minecraft client, FriendlyByteBuf buf)
    {
        Map<String, Integer> actors = new HashMap<>();
        String filmId = buf.readString();

        for (int i = 0, c = buf.readInt(); i < c; i++)
        {
            String key = buf.readString();
            int entityId = buf.readInt();

            actors.put(key, entityId);
        }

        client.execute(() ->
        {
            UIDashboard dashboard = BBSModClient.getDashboard();
            UIFilmPanel panel = dashboard.getPanel(UIFilmPanel.class);

            panel.updateActors(filmId, actors);
            BBSModClient.getFilms().updateActors(filmId, actors);
        });
    }

    private static void handleGunPropertiesPacket(Minecraft client, FriendlyByteBuf buf)
    {
        GunProperties properties = new GunProperties();
        int entityId = buf.readInt();

        properties.fromNetwork(buf);

        client.execute(() ->
        {
            Entity entity = client.world.getEntityById(entityId);

            if (entity instanceof GunProjectileEntity projectile)
            {
                projectile.setProperties(properties);
                projectile.calculateDimensions();
            }
        });
    }

    private static void handlePauseFilmPacket(Minecraft client, FriendlyByteBuf buf)
    {
        String filmId = buf.readString();

        client.execute(() ->
        {
            Films.togglePauseFilm(filmId);
        });
    }

    private static void handleSelectedSlotPacket(Minecraft client, FriendlyByteBuf buf)
    {
        int slot = buf.readInt();

        client.execute(() ->
        {
            client.player.getInventory().selectedSlot = slot;
        });
    }

    private static void handleAnimationStateModelBlockPacket(Minecraft client, FriendlyByteBuf buf)
    {
        BlockPos pos = buf.readBlockPos();
        String state = buf.readString();

        client.execute(() ->
        {
            BlockEntity blockEntity = client.world.getBlockEntity(pos);

            if (blockEntity instanceof ModelBlockEntity block)
            {
                if (block.getProperties().getForm() != null)
                {
                    block.getProperties().getForm().playState(state);
                }
            }
        });
    }

    private static void handleRefreshModelBlocksPacket(Minecraft client, FriendlyByteBuf buf)
    {
        int range = buf.readInt();

        client.execute(() ->
        {
            for (ModelBlockEntity mb : BBSRendering.capturedModelBlocks)
            {
                ModelProperties properties = mb.getProperties();
                int random = (int) (Math.random() * range);

                properties.setForm(FormUtils.copy(properties.getForm()));

                while (random > 0)
                {
                    properties.update(mb.getEntity());

                    random -= 1;
                }
            }
        });
    }

    /* API */
    
    public static void sendModelBlockForm(BlockPos pos, ModelBlockEntity modelBlock)
    {
        crusher.send(Minecraft.getInstance().player, ServerNetwork.SERVER_MODEL_BLOCK_FORM_PACKET, modelBlock.getProperties().toData(), (packetByteBuf) ->
        {
            packetByteBuf.writeBlockPos(pos);
        });
    }

    public static void sendPlayerForm(Form form)
    {
        MapType mapType = FormUtils.toData(form);

        crusher.send(Minecraft.getInstance().player, ServerNetwork.SERVER_PLAYER_FORM_PACKET, mapType == null ? new MapType() : mapType, (packetByteBuf) ->
        {});
    }

    public static void sendModelBlockTransforms(MapType data)
    {
        crusher.send(Minecraft.getInstance().player, ServerNetwork.SERVER_MODEL_BLOCK_TRANSFORMS_PACKET, data, (packetByteBuf) ->
        {});
    }

    public static void sendManagerDataLoad(String id, Consumer<BaseType> consumer)
    {
        MapType mapType = new MapType();

        mapType.putString("id", id);
        ClientNetwork.sendManagerData(RepositoryOperation.LOAD, mapType, consumer);
    }

    public static void sendManagerData(RepositoryOperation op, BaseType data, Consumer<BaseType> consumer)
    {
        int id = ids;

        callbacks.put(id, consumer);
        sendManagerData(id, op, data);

        ids += 1;
    }

    public static void sendManagerData(int callbackId, RepositoryOperation op, BaseType data)
    {
        crusher.send(Minecraft.getInstance().player, ServerNetwork.SERVER_MANAGER_DATA_PACKET, data, (packetByteBuf) ->
        {
            packetByteBuf.writeInt(callbackId);
            packetByteBuf.writeInt(op.ordinal());
        });
    }

    public static void sendActionRecording(String filmId, int replayId, int tick, int countdown, boolean state)
    {
        FriendlyByteBuf buf = FriendlyByteBufs.create();

        buf.writeString(filmId);
        buf.writeInt(replayId);
        buf.writeInt(tick);
        buf.writeInt(countdown);
        buf.writeBoolean(state);

        ClientPlayNetworking.send(ServerNetwork.SERVER_ACTION_RECORDING, buf);
    }

    public static void sendToggleFilm(String filmId, boolean withCamera)
    {
        FriendlyByteBuf buf = FriendlyByteBufs.create();

        buf.writeString(filmId);
        buf.writeBoolean(withCamera);

        ClientPlayNetworking.send(ServerNetwork.SERVER_TOGGLE_FILM, buf);
    }

    public static void sendActionState(String filmId, ActionState state, int tick)
    {
        FriendlyByteBuf buf = FriendlyByteBufs.create();

        buf.writeString(filmId);
        buf.writeByte(state.ordinal());
        buf.writeInt(tick);

        ClientPlayNetworking.send(ServerNetwork.SERVER_ACTION_CONTROL, buf);
    }

    public static void sendSyncData(String filmId, BaseValue data)
    {
        crusher.send(Minecraft.getInstance().player, ServerNetwork.SERVER_FILM_DATA_SYNC, data.toData(), (packetByteBuf) ->
        {
            DataPath path = data.getPath();

            packetByteBuf.writeString(filmId);
            packetByteBuf.writeInt(path.strings.size());

            for (String string : path.strings)
            {
                packetByteBuf.writeString(string);
            }
        });
    }

    public static void sendTeleport(Player entity, double x, double y, double z)
    {
        sendTeleport(x, y, z, entity.getHeadYaw(), entity.getHeadYaw(), entity.getPitch());
    }

    public static void sendTeleport(double x, double y, double z, float yaw, float bodyYaw, float pitch)
    {
        FriendlyByteBuf buf = FriendlyByteBufs.create();

        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeFloat(yaw);
        buf.writeFloat(bodyYaw);
        buf.writeFloat(pitch);

        ClientPlayNetworking.send(ServerNetwork.SERVER_PLAYER_TP, buf);
    }

    public static void sendFormTrigger(String triggerId, int type)
    {
        FriendlyByteBuf buf = FriendlyByteBufs.create();

        buf.writeString(triggerId);
        buf.writeInt(type);

        ClientPlayNetworking.send(ServerNetwork.SERVER_ANIMATION_STATE_TRIGGER, buf);
    }

    public static void sendSharedForm(Form form, UUID uuid)
    {
        MapType mapType = FormUtils.toData(form);

        crusher.send(Minecraft.getInstance().player, ServerNetwork.SERVER_SHARED_FORM, mapType == null ? new MapType() : mapType, (packetByteBuf) ->
        {
            packetByteBuf.writeUuid(uuid);
        });
    }

    public static void sendZoom(boolean zoom)
    {
        FriendlyByteBuf buf = FriendlyByteBufs.create();

        buf.writeBoolean(zoom);

        ClientPlayNetworking.send(ServerNetwork.SERVER_ZOOM, buf);
    }

    public static void sendPauseFilm(String filmId)
    {
        FriendlyByteBuf buf = FriendlyByteBufs.create();

        buf.writeString(filmId);

        ClientPlayNetworking.send(ServerNetwork.SERVER_PAUSE_FILM, buf);
    }
}


