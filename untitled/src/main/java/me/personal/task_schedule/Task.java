package me.personal.task_schedule;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Task implements Comparable<Task> {
    private String name;
    private Integer priority;

    @Override
    public int compareTo(Task other) {
        return this.priority.compareTo(other.priority);
    }

    @Override
    public String toString() {
        return String.format("Task %s", name);
    }
}