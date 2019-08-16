package wailord2.betterannouncements.betterannouncements;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public final class BetterAnnouncements extends JavaPlugin implements CommandExecutor {

    FileConfiguration config;
    File cFile;
    Path datapath;
    Timer timer = new Timer();

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("BetterAnnouncements - BetterAnnouncements is now enabled");
        saveConfig();
        config = getConfig();
        cFile = new File(getDataFolder(), "config.yml");
        datapath = Paths.get(getDataFolder().getPath(), "announcements.txt");
        if(config.get("interval") == null){
            config.set("interval", 100);
            config.set("usePermissions", false);
            saveConfig();
            File data = getDataFolder();
            try {
                Files.createFile(datapath);
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(datapath.toString()))) {

                    bw.write("§4Example announcement");
                    bw.newLine();
                    bw.write("§4This is part of the example");
                    bw.newLine();
                    bw.write("~");
                    bw.newLine();
                    bw.write("§3This is another announcement");
                    bw.newLine();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            getAnnouncements(datapath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }


    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
        if(cmd.getName().equalsIgnoreCase("bareload")){

            if(sender.hasPermission("betterannouncements.use")){
                config  = YamlConfiguration.loadConfiguration(cFile);
                getServer().getConsoleSender().sendMessage("BetterAnnouncer - Reloaded config");
                try {
                    timer.cancel();
                    getAnnouncements(datapath);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }

        }
        return true;
    }

    private void getAnnouncements(Path path) throws FileNotFoundException {
        List<List<String>> announcements = new ArrayList<>();

        List<String> currentAnnouncement = new ArrayList<>();

        Scanner scanner = new Scanner(new File(path.toString()));
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            if(line.equalsIgnoreCase("~")){
                announcements.add(currentAnnouncement);
                currentAnnouncement = new ArrayList<>();
            }
            else{
                currentAnnouncement.add(line);
            }
        }
        announcements.add(currentAnnouncement);

        startAnnouncing(announcements);
    }

    private void startAnnouncing(List<List<String>> announcements){
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            int iterator = 0;
            @Override
            public void run() {
                List<String> announcement = announcements.get(iterator);


                if((boolean)config.get("usePermissions")){
                    String perm = announcement.get(announcement.size()-1);

                    for(int i = 0; i<announcement.size(); i++){
                        if(i != announcement.size()-1){
                            Bukkit.broadcast(announcement.get(i), perm);
                        }
                    }
                }else{
                    for (String line : announcement) {
                        Bukkit.broadcastMessage(line);
                    }
                }

//                for (String line : announcement) {
//                    Bukkit.broadcastMessage(line);
//                }

                iterator++;
                if(iterator >= announcements.size()){
                    iterator = 0;
                }
            }
        }, 0, (int)config.get("interval")*1000);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("BetterAnnouncements - BetterAnnouncements is now disabled");
        saveConfig();
    }


}
