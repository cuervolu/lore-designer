import { invoke } from '@tauri-apps/api/core'
import { listen, type EventCallback } from "@tauri-apps/api/event";

/**
 * Represents a log entry sent from the backend.
 */
export interface LogEntry {
  timestamp: string;
  level: string;
  target: string;
  message: string;
}

/**
 * Options for the log commands.
 */
export interface LogOptions {
  /** The log target */
  target?: string;
}

/**
 * Emits a TRACE level log from the frontend to the backend.
 * @param message The message to log.
 * @param options Additional options like 'target'.
 */
export async function trace(
  message: string,
  options?: LogOptions,
): Promise<void> {
  await invoke("plugin:tracing|log_trace", {
    message,
    target: options?.target,
  });
}

/**
 * Emits a DEBUG level log from the frontend to the backend.
 * @param message The message to log.
 * @param options Additional options like 'target'.
 */
export async function debug(
  message: string,
  options?: LogOptions,
): Promise<void> {
  await invoke("plugin:tracing|log_debug", {
    message,
    target: options?.target,
  });
}

/**
 * Emits an INFO level log from the frontend to the backend.
 * @param message The message to log.
 * @param options Additional options like 'target'.
 */
export async function info(
  message: string,
  options?: LogOptions,
): Promise<void> {
  await invoke("plugin:tracing|log_info", {
    message,
    target: options?.target,
  });
}

/**
 * Emits a WARN level log from the frontend to the backend.
 * @param message The message to log.
 * @param options Additional options like 'target'.
 */
export async function warn(
  message: string,
  options?: LogOptions,
): Promise<void> {
  await invoke("plugin:tracing|log_warn", {
    message,
    target: options?.target,
  });
}

/**
 * Emits an ERROR level log from the frontend to the backend.
 * @param message The message to log.
 * @param options Additional options like 'target'.
 */
export async function error(
  message: string,
  options?: LogOptions,
): Promise<void> {
  await invoke("plugin:tracing|log_error", {
    message,
    target: options?.target,
  });
}

/**
 * Listens for log events (in batches) emitted from the backend.
 * @param callback The function to call when a batch of logs is received.
 * @returns A promise that resolves to an unlisten function.
 */
export async function listenToLogs(
  callback: EventCallback<LogEntry[]>,
): Promise<() => void> {
  return await listen<LogEntry[]>("log:entry", callback);
}

/**
 * Gets the current log history stored in the backend buffer.
 * @returns A promise that resolves to an array of LogEntry.
 */
export async function getHistory(): Promise<LogEntry[]> {
  return await invoke("plugin:tracing|get_log_history");
}

/**
 * Clears the log buffer on the backend.
 */
export async function clearHistory(): Promise<void> {
  await invoke("plugin:tracing|clear_log_buffer");
}