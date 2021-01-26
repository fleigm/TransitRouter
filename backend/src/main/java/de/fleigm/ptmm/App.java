package de.fleigm.ptmm;

import de.fleigm.ptmm.data.DataRoot;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import one.microstream.jdk8.java.util.BinaryHandlersJDK8;
import one.microstream.reflect.ClassLoaderProvider;
import one.microstream.storage.configuration.Configuration;
import one.microstream.storage.types.EmbeddedStorageFoundation;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.storage.types.StorageManager;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import java.nio.file.Path;

@ApplicationScoped
public class App {
  private static final Logger logger = LoggerFactory.getLogger(App.class);

  private static App instance = null;

  public static App getInstance() {
    return instance;
  }

  private volatile EmbeddedStorageManager storageManager = null;

  @ConfigProperty(name = "app.storage")
  Path storagePath = Path.of("storage");

  public App() {
    App.instance = this;
  }

  public App(Path storagePath) {
    this();
    this.storagePath = storagePath;
  }

  private EmbeddedStorageManager createStorageManager() {

    final Configuration configuration = Configuration.Default()
        .setBaseDirectory(storagePath.toString())
        .setChannelCount(2);

    final EmbeddedStorageFoundation<?> foundation = configuration
        .createEmbeddedStorageFoundation()
        .onConnectionFoundation(BinaryHandlersJDK8::registerJDK8TypeHandlers)
        .onConnectionFoundation(cf ->
            cf.setClassLoaderProvider(ClassLoaderProvider.New(Thread.currentThread().getContextClassLoader())));

    final EmbeddedStorageManager storageManager = foundation.createEmbeddedStorageManager().start();

    if (storageManager.root() == null) {
      final DataRoot data = new DataRoot();
      storageManager.setRoot(data);
      storageManager.storeRoot();
      logger.info("created new store");
    } else {
      logger.info("loaded existing store");
    }

    return storageManager;
  }

  public StorageManager storageManager() {
    /*
     * Double-checked locking to reduce the overhead of acquiring a lock
     * by testing the locking criterion.
     * The field (this.storageManager) has to be volatile.
     */
    if (this.storageManager == null) {
      synchronized (this) {
        if (this.storageManager == null) {
          this.storageManager = this.createStorageManager();
        }
      }
    }

    return this.storageManager;
  }

  @Produces
  @ApplicationScoped
  public DataRoot data() {
    return (DataRoot) storageManager().root();
  }

  public void shutdown() {
    storageManager().shutdown();
  }

  public void init(@Observes StartupEvent event) {
    storageManager();
    logger.info("Initialized storage manager");
  }

  public void exit(@Observes ShutdownEvent event) {
    shutdown();
    logger.info("shutdown storage manager");
  }
}
