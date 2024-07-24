import {info as tauriInfo, error as tauriError} from '@tauri-apps/plugin-log';

type LogFunction = (...args: any[]) => Promise<void>;


interface LogObject {
  info: LogFunction;
  error: LogFunction;
}


const asyncConsoleLog: LogFunction = async (...args) => {
  console.log(...args);
};

const asyncConsoleError: LogFunction = async (...args) => {
  console.error(...args);
};


export function createLogObject(): LogObject {
  let infoFn: LogFunction = asyncConsoleLog;
  let errorFn: LogFunction = asyncConsoleError;

  if (process.env.NODE_ENV === 'production') {
    infoFn = async (...args) => {
      await tauriInfo(args.join(' '));
    };
    errorFn = async (...args) => {
      await tauriError(args.join(' '));
    };
  }

  return {
    info: infoFn,
    error: errorFn,
  };
}


export const log = createLogObject();