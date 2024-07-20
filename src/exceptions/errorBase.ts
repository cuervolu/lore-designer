/**
 * Represents a base class for custom error types.
 * @template T - The type of the error name.
 */
export class ErrorBase<T extends string> extends Error {
  /**
   * The name of the error.
   */
  name: T;
  /**
   * The error message, which is a human-readable description of the error.
   */
  message: string;
  /**
   * The cause of the error, which is the original error that caused this error.
   */
  cause: unknown;

  /**
   * Creates an instance of ErrorBase.
   * @param {Object} params - The parameters for creating the error.
   * @param {T} params.name - The name of the error.
   * @param {string} params.message - The error message.
   * @param {unknown} params.cause - The cause of the error.
   */
  constructor({ name, message, cause }: { name: T; message: string; cause: unknown }) {
    super();
    this.name = name;
    this.message = message;
    this.cause = cause;
  }
}
