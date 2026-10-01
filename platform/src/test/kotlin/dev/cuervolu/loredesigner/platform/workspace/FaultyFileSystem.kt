package dev.cuervolu.loredesigner.platform.workspace

import okio.FileSystem
import okio.ForwardingFileSystem
import okio.Path
import okio.Sink
import okio.Source

/** Runs [beforeOperation] ahead of each mutating or reading call, so tests can fail or race it. */
internal class FaultyFileSystem(delegate: FileSystem) : ForwardingFileSystem(delegate) {
    enum class Operation { CREATE_DIRECTORY, SINK, ATOMIC_MOVE, DELETE, SOURCE }

    var beforeOperation: (Operation, Path) -> Unit = { _, _ -> }

    override fun createDirectory(dir: Path, mustCreate: Boolean) {
        beforeOperation(Operation.CREATE_DIRECTORY, dir)
        super.createDirectory(dir, mustCreate)
    }

    override fun sink(file: Path, mustCreate: Boolean): Sink {
        beforeOperation(Operation.SINK, file)
        return super.sink(file, mustCreate)
    }

    override fun atomicMove(source: Path, target: Path) {
        beforeOperation(Operation.ATOMIC_MOVE, target)
        super.atomicMove(source, target)
    }

    override fun delete(path: Path, mustExist: Boolean) {
        beforeOperation(Operation.DELETE, path)
        super.delete(path, mustExist)
    }

    override fun source(file: Path): Source {
        beforeOperation(Operation.SOURCE, file)
        return super.source(file)
    }
}
