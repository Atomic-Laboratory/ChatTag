package chatTag;

import arc.Core;
import arc.Events;
import arc.struct.StringMap;
import arc.util.CommandHandler;
import arc.util.Log;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.mod.Plugin;

public class ChatTag extends Plugin {
    private static StringMap playerTags = new StringMap();
    @Override
    public void init() {
        playerTags = Core.settings.getJson("player-tags", StringMap.class, StringMap::new);

        Events.on(EventType.ServerLoadEvent.class, event -> {
            final var old = Vars.netServer.chatFormatter;
            Vars.netServer.chatFormatter = (player, message) -> {
                var other = old.format(player, message);
                if (player == null) return other;

                var tag = playerTags.get(player.uuid());
                return (tag == null ? "" : "[#ff7f50][[[white]" + tag + "[#ff7f50]][white] ") + other;
            };
        });
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        handler.register("tag-add", "<ID> <Tag...>", "Add a player tag.", arg -> {
            var info = Vars.netServer.admins.getInfoOptional(arg[0]);
            if (info == null) {
                Log.err("This ID not found!");
                return;
            }
            playerTags.put(arg[0], arg[1].trim());
            save();
            Log.info("The tag has been added.");
        });

        handler.register("tag-remove", "<ID>", "Remove a player tag.", arg -> {
            var info = Vars.netServer.admins.getInfoOptional(arg[0]);
            if (info == null) {
                Log.err("This ID not found!");
                return;
            }
            playerTags.remove(arg[0]);
            save();
            Log.info("The tag has been removed.");
        });

        handler.register("tag-list", "List all player tags.", arg -> {
            Log.info("List Tags:");
            for (var e : playerTags)
                Log.info("\t@ (@): @", Vars.netServer.admins.getInfo(e.key).lastName, e.key, e.value);
        });
    }

    private static void save() {
        Core.settings.putJson("player-tags", playerTags);
        Core.settings.forceSave();
    }
}
