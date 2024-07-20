// exceptions/db.error.ts
import { ErrorBase } from './errorBase';

type ErrorName = 'DB_CONNECTION_ERROR' | 'DB_QUERY_ERROR' | 'DB_INITIALIZATION_ERROR';

export class DatabaseError extends ErrorBase<ErrorName> {}