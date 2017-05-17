package de.linzn.aiCore;

import de.linzn.aiCore.database.DatabaseModule;
import de.linzn.aiCore.internal.skillsApi.SkillApi;
import de.linzn.aiCore.processing.MainProcessingModule;
import de.linzn.aiCore.processing.network.NetworkModule;
import de.linzn.aiCore.processing.terminal.TerminalModule;
import de.linzn.aiCore.settings.FileSettings;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class App {
    // The main instance
    public static App appInstance;
    // Define new variables for later
    public FileSettings aiSettings;
    public MainProcessingModule inputProc;
    public NetworkModule networkProc;
    public TerminalModule terminalProc;
    public DatabaseModule mysqlData;
    public SkillApi skillApi;
    public Heartbeat heartbeat;
    private long start_time;

    // The alive value for the heartbeat thread
    public AtomicBoolean isAlive;
    // The task list for next heartbeat
    public LinkedList<Runnable> taskList = new LinkedList<Runnable>();

    // Main for init this framework
    public static void main(String[] args) {
        App.logger("Creating new App instance.");
        new App(args);
    }

    // The default logger
    public static synchronized void logger(String log) {
        System.out.print("[" + Thread.currentThread().getName() + "] " + log + "\n");
        System.out.flush();
    }

    // The instance
    public App(String[] args) {
        this.start_time = System.nanoTime();
        this.isAlive = new AtomicBoolean();
        appInstance = this;
        this.heartbeat = new Heartbeat(appInstance);
        Thread heart = new Thread(this.heartbeat);
        heart.start();
        loadModules();
        finishStartup();

    }

    // Load the modules for the framework
    private void loadModules() {

        Runnable settings = new Runnable() {
            @Override
            public void run() {
                aiSettings = new FileSettings(appInstance);
            }

        };
        Runnable mysql = new Runnable() {
            @Override
            public void run() {
                mysqlData = new DatabaseModule(appInstance);
            }

        };
        Runnable input = new Runnable() {
            @Override
            public void run() {
                inputProc = new MainProcessingModule(appInstance);
            }

        };
        Runnable network = new Runnable() {
            @Override
            public void run() {
                networkProc = new NetworkModule(appInstance);
            }

        };
        Runnable terminal = new Runnable() {
            @Override
            public void run() {
                terminalProc = new TerminalModule(appInstance);
            }

        };

        Runnable skill = new Runnable() {
            @Override
            public void run() {
                skillApi = new SkillApi(appInstance);
            }

        };

        this.runTaskSync(settings);
        this.runTaskSync(mysql);
        this.runTaskSync(input);
        this.runTaskSync(network);
        this.runTaskSync(terminal);
        this.runTaskSync(skill);
    }

    private void finishStartup() {
        Runnable finish = new Runnable() {
            @Override
            public void run() {
                logger("Startup finished in " + (int) ((System.nanoTime() - start_time) / 1e6) + " ms.");
            }

        };
        this.runTaskSync(finish);
    }

    // Add a sync task to the heartbeat tasklist
    public void runTaskSync(Runnable sync) {
        this.taskList.add(sync);
    }

    // add a async task to the heartbeat tasklist
    public void runTaskAsync(Runnable async) {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                new ThreadPoolExecutor(1, 1, 250L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>())
                        .submit(async);
            }

        };

        this.taskList.add(runnable);
    }

}
