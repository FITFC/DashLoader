package dev.quantumfusion.dashloader.progress;

import dev.quantumfusion.dashloader.DashLoader;
import dev.quantumfusion.taski.Task;
import dev.quantumfusion.taski.builtin.StaticTask;

import java.util.HashMap;

public final class ProgressHandler {
	public static Task TASK = new StaticTask("Idle", 0);
	private String currentTask;

	private long lastUpdate = System.currentTimeMillis();
	private double currentProgress = 0;

	private HashMap<String, String> translations = new HashMap<>();

	public ProgressHandler() {
	}

	public void setTranslations(HashMap<String, String> translations) {
		this.translations = translations;
	}

	private void tickProgress() {
		final double actualProgress = TASK.getProgress();
		final double divisionSpeed = (actualProgress < this.currentProgress) ? 3 : DashLoader.INSTANCE.config.config.progressBarSpeedDivision;
		this.currentProgress += (actualProgress - this.currentProgress) / divisionSpeed;
	}

	public double getProgress() {
		final long currentTime = System.currentTimeMillis();
		while (currentTime > this.lastUpdate) {
			this.tickProgress();
			this.lastUpdate += 10; // ~100ups
		}
		return this.currentProgress;
	}

	public String getCurrentTask() {
		return this.currentTask;
	}

	public ProgressHandler setCurrentTask(String currentTask) {
		this.currentTask = this.translations.getOrDefault(currentTask, currentTask);
		return this;
	}
}
