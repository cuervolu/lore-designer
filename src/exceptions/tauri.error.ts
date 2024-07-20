import { ErrorBase } from './errorBase';

type ErrorName = 'CANNOT_CREATE_WINDOW' | 'OAUTH_PLUGIN_ERROR' | 'FAILED_TO_SET_USER';

export class TauriError extends ErrorBase<ErrorName> {}
