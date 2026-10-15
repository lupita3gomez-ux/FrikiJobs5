package com.tuempresa.jobs.listener;

import com.tuempresa.jobs.JobsPlugin;
import com.tuempresa.jobs.gui.JobsGUI;
import com.tuempresa.jobs.model.Job;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();
        if (!title.contains("Selección de Trabajos") && !title.contains("📋")) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String displayName = clicked.getItemMeta().getDisplayName();

        if (title.contains("Selección de Trabajos")) {
            if (displayName.contains("Minero")) {
                JobsGUI.openJobDetailMenu(player, "minero");
            } else if (displayName.contains("Granjero")) {
                JobsGUI.openJobDetailMenu(player, "granjero");
            } else if (displayName.contains("Encantador")) {
                JobsGUI.openJobDetailMenu(player, "hechicero");
            } else if (displayName.contains("Pescador")) {
                JobsGUI.openJobDetailMenu(player, "pescador");
            } else if (displayName.contains("Guerrero")) {
                JobsGUI.openJobDetailMenu(player, "guerrero");
            } else if (displayName.contains("Leñador")) {
                JobsGUI.openJobDetailMenu(player, "lenador");
            } else if (displayName.contains("Alquimista")) {
                JobsGUI.openJobDetailMenu(player, "alquimista");
            } else if (displayName.contains("Abandonar Trabajo")) {
                double penalty = JobsPlugin.getInstance().getConfig().getDouble("quit_penalty", 50.0);
                var econ = JobsPlugin.getInstance().getEconomyManager();

                if (econ != null && econ.hasAccount(player)) {
                    if (econ.getBalance(player) < penalty) {
                        player.sendMessage(ChatColor.RED + "No tienes suficiente dinero para pagar la multa de renuncia ($" + penalty + ").");
                        player.closeInventory();
                        return;
                    }
                    econ.withdrawPlayer(player, penalty);
                }

                JobsPlugin.getInstance().getPlayerJobManager().setPlayerJob(player.getUniqueId(), null);
                player.sendMessage(ChatColor.RED + "Has abandonado tu trabajo. Se te han descontado $" + penalty + " de multa.");
                player.closeInventory();
            }
        } else if (title.contains("📋")) {
            if (displayName.contains("Volver al Menú Principal")) {
                JobsGUI.openJobsMenu(player);
            } else if (displayName.contains("Unirse a")) {
                String jobId = null;
                if (title.contains("Minero")) jobId = "minero";
                else if (title.contains("Granjero")) jobId = "granjero";
                else if (title.contains("Hechicero")) jobId = "hechicero";
                else if (title.contains("Pescador")) jobId = "pescador";
                else if (title.contains("Guerrero")) jobId = "guerrero";
                else if (title.contains("Leñador")) jobId = "lenador";
                else if (title.contains("Alquimista")) jobId = "alquimista";

                if (jobId != null) {
                    com.tuempresa.jobs.manager.PlayerJobManager pjm = JobsPlugin.getInstance().getPlayerJobManager();
                    String currentJob = pjm.getPlayerJob(player.getUniqueId());

                    if (jobId.equalsIgnoreCase(currentJob)) {
                        player.sendMessage(ChatColor.YELLOW + "Ya estás en este trabajo.");
                        player.closeInventory();
                        return;
                    }

                    double penalty = JobsPlugin.getInstance().getConfig().getDouble("quit_penalty", 50.0);
                    var econ = JobsPlugin.getInstance().getEconomyManager();

                    if (currentJob != null) {
                        if (econ != null && econ.hasAccount(player)) {
                            if (econ.getBalance(player) < penalty) {
                                player.sendMessage(ChatColor.RED + "No tienes suficiente dinero para pagar la multa por cambiar de trabajo ($" + penalty + ").");
                                player.closeInventory();
                                return;
                            }
                            econ.withdrawPlayer(player, penalty);
                        }
                    }

                    pjm.setPlayerJob(player.getUniqueId(), jobId);
                    Job job = JobsPlugin.getInstance().getJobManager().getJob(jobId);
                    String jobName = job != null ? job.getName() : jobId;

                    if (currentJob != null) {
                        player.sendMessage(ChatColor.GREEN + "¡Te has cambiado al trabajo de " + jobName + "! (Multa pagada: $" + penalty + ")");
                    } else {
                        player.sendMessage(ChatColor.GREEN + "¡Te has unido exitosamente al trabajo de " + jobName + "!");
                    }
                    player.closeInventory();
                }
            }
        }
    }
}
