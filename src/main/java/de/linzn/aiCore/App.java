package de.linzn.aiCore;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import de.linzn.aiCore.database.MySQLDatabase;
import de.linzn.aiCore.inputProcessing.InputProcessing;
import de.linzn.aiCore.inputProcessing.network.NetworkProcessing;
import de.linzn.aiCore.inputProcessing.terminal.TerminalProcessing;
import de.linzn.aiCore.settings.AiSettings;

public class App {

	public static App appInstance;

	public AiSettings aiSettings;
	public InputProcessing inputProc;
	public NetworkProcessing networkProc;
	public TerminalProcessing terminalProc;
	public MySQLDatabase mysqlData;
	public Heartbeat heartbeat;

	public boolean isAlive;
	public LinkedList<Runnable> taskList = new LinkedList<Runnable>();

	public static void main(String[] args) {
		App.logger("Creating new App instance.");
		appInstance = new App(args);
	}

	public static void logger(String log) {
		System.out.println("[AiCore]: " + log);
	}

	public App(String[] args) {
		loadModules();
	}

	private void loadModules() {
		this.aiSettings = new AiSettings(appInstance);
		this.inputProc = new InputProcessing(appInstance);
		this.networkProc = new NetworkProcessing(appInstance);
		this.terminalProc = new TerminalProcessing(appInstance);
		this.mysqlData = new MySQLDatabase(appInstance);
		this.heartbeat = new Heartbeat(appInstance);
		new Thread(this.heartbeat).start();
	}

	public void runTaskSync(Runnable sync) {
		this.taskList.add(sync);
	}

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