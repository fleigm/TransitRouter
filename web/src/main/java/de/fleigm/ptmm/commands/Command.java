package de.fleigm.ptmm.commands;

@FunctionalInterface
public interface Command<T> {

  CommandResult execute(T payload);
}
