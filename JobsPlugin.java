package com.tuempresa.jobs;

import com.tuempresa.jobs.gui.JobsGUI;
import com.tuempresa.jobs.listener.GUIListener;
import com.tuempresa.jobs.listener.JobActionListener;
import com.tuempresa.jobs.manager.EconomyManager;
import com.tuempresa.jobs.manager.JobManager;
import com.tuempresa.jobs.manager.PlayerJobManager;
import com.tuempresa.jobs.model.Job;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class JobsPlugin extends JavaPlugin {

    private static JobsPlugin instance;
    private JobManager jobManager;
    private EconomyManager economyManager;
    private PlayerJobManager playerJobManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.economyManager = new EconomyManager();
        if (!economyManager.setupEconomy()) {
            getLogger().warning("Vault no encontrado. La integración económica se deshabilitó.");
        }

        this.jobManager = new JobManager(this);
        jobManager.loadJobs();

        this.playerJobManager = new PlayerJobManager(this);

        // Registrar eventos, menú y protección anti-exploit
        getServer().getPluginManager().registerEvents(new JobActionListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(), this);

        getLogger().info("JobsPlugin inicializado correctamente con comandos de administración.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("jobs")) {
            // Comandos de Administración (/jobs set /jobs remove)
            if (args.length > 0) {
                if (!sender.hasPermission("jobs.admin")) {
                    sender.sendMessage(ChatColor.RED + "No tienes permiso para ejecutar comandos administrativos de trabajos.");
                    return true;
                }

                String subCommand = args[0].toLowerCase();

                if (subCommand.equals("set")) {
                    if (args.length < 3) {
                        sender.sendMessage(ChatColor.RED + "Uso correcto: /jobs set <jugador> <trabajo>");
                        return true;
                    }

                    OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                    if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
                        sender.sendMessage(ChatColor.RED + "El jugador '" + args[1] + "' no ha sido encontrado.");
                        return true;
                    }

                    String jobId = args[2].toLowerCase();
                    Job job = jobManager.getJob(jobId);
                    if (job == null) {
                        sender.sendMessage(ChatColor.RED + "El trabajo '" + args[2] + "' no existe. Trabajos válidos: minero, granjero, hechicero, pescador, guerrero, lenador, alquimista.");
                        return true;
                    }

                    playerJobManager.setPlayerJob(target.getUniqueId(), jobId);
                    sender.sendMessage(ChatColor.GREEN + "Se ha asignado el trabajo de " + job.getName() + " a " + target.getName() + " correctamente (Sin costo ni multa).");

                    if (target.isOnline()) {
                        ((Player) target).sendMessage(ChatColor.GOLD + "Un administrador te ha asignado el trabajo de " + job.getName() + ".");
                    }
                    return true;
                }

                if (subCommand.equals("remove") || subCommand.equals("clear")) {
                    if (args.length < 2) {
                        sender.sendMessage(ChatColor.RED + "Uso correcto: /jobs remove <jugador>");
                        return true;
                    }

                    OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                    if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
                        sender.sendMessage(ChatColor.RED + "El jugador '" + args[1] + "' no ha sido encontrado.");
                        return true;
                    }

                    String currentJob = playerJobManager.getPlayerJob(target.getUniqueId());
                    if (currentJob == null) {
                        sender.sendMessage(ChatColor.YELLOW + "El jugador " + target.getName() + " no tiene ningún trabajo asignado.");
                        return true;
                    }

                    playerJobManager.setPlayerJob(target.getUniqueId(), null);
                    sender.sendMessage(ChatColor.GREEN + "Se le ha retirado el trabajo a " + target.getName() + " correctamente (Sin cobrar multa).");

                    if (target.isOnline()) {
                        ((Player) target).sendMessage(ChatColor.RED + "Un administrador te ha retirado tu trabajo actual.");
                    }
                    return true;
                }

                sender.sendMessage(ChatColor.RED + "Subcomando desconocido. Usos administrativos:");
                sender.sendMessage(ChatColor.YELLOW + "/jobs set <jugador> <trabajo>");
                sender.sendMessage(ChatColor.YELLOW + "/jobs remove <jugador>");
                return true;
            }

            // Abrir menú GUI normal para jugadores
            if (sender instanceof Player player) {
                JobsGUI.openJobsMenu(player);
                return true;
            } else {
                sender.sendMessage("Uso en consola: /jobs set <jugador> <trabajo> o /jobs remove <jugador>");
            }
        }
        return false;
    }

    public static JobsPlugin getInstance() {
        return instance;
    }

    public JobManager getJobManager() {
        return jobManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public PlayerJobManager getPlayerJobManager() {
        return playerJobManager;
    }
}
