package de.fleigm.transitrouter.commands;

@FunctionalInterface
public interface Command<T> {

  CommandResult execute(T payload);
}
