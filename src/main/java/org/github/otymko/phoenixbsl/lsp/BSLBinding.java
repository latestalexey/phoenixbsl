package org.github.otymko.phoenixbsl.lsp;

import com.google.common.annotations.VisibleForTesting;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageServer;
import org.github.otymko.phoenixbsl.core.PhoenixAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class BSLBinding {

  private static final Logger LOGGER = LoggerFactory.getLogger(BSLBinding.class.getSimpleName());

  private BSLLanguageClient client;
  private LanguageServer server;
  private InputStream in;
  private OutputStream out;
  private Launcher<LanguageServer> launcher;
  private Thread thread = new Thread(this::start);

  public BSLBinding(BSLLanguageClient client, InputStream in, OutputStream out) {
    this.client = client;
    this.in = in;
    this.out = out;
  }

  public void startInThread() {
    LOGGER.info("Подключение к серверу LSP");
    thread.setDaemon(true);
    thread.setName("BSLLanguageLauncher");
    thread.start();
  }

  @VisibleForTesting
  private void start() {

    launcher = LSPLauncher.createClientLauncher(client, in, out);
    Future<?> future = launcher.startListening();

    server = launcher.getRemoteProxy();

    while (true) {
      try {
        future.get();
        return;
      } catch (InterruptedException e) {
        LOGGER.error(e.getMessage().toString());
      } catch (ExecutionException e) {
        LOGGER.error(e.getMessage().toString());
      }
    }

  }

  public CompletableFuture<InitializeResult> initialize() {
    var params = new InitializeParams();
    params.setProcessId(PhoenixAPI.getProcessId());
    params.setTrace("messages");
    ClientCapabilities serverCapabilities = new ClientCapabilities();
    params.setCapabilities(serverCapabilities);
    return server.initialize(params);
  }

  public void textDocumentDidOpen(URI uri, String text) {
    DidOpenTextDocumentParams params = new DidOpenTextDocumentParams();
    TextDocumentItem item = new TextDocumentItem();
    item.setLanguageId("bsl");
    item.setUri(uri.toString());
    item.setText(text);
    params.setTextDocument(item);
    server.getTextDocumentService().didOpen(params);
  }

  public void textDocumentDidChange(URI uri, String text) {
    var params = new DidChangeTextDocumentParams();
    VersionedTextDocumentIdentifier versionedTextDocumentIdentifier = new VersionedTextDocumentIdentifier();
    versionedTextDocumentIdentifier.setUri(uri.toString());
    versionedTextDocumentIdentifier.setVersion(1);
    params.setTextDocument(versionedTextDocumentIdentifier);
    var textDocument = new TextDocumentContentChangeEvent();
    textDocument.setText(text);
    List<TextDocumentContentChangeEvent> list = new ArrayList<>();
    list.add(textDocument);
    params.setContentChanges(list);
    server.getTextDocumentService().didChange(params);
  }

  public void textDocumentDidSave(URI uri) {
    var paramsSave = new DidSaveTextDocumentParams();
    TextDocumentIdentifier textDocumentIdentifier = new TextDocumentIdentifier();
    textDocumentIdentifier.setUri(uri.toString());
    paramsSave.setTextDocument(textDocumentIdentifier);
    server.getTextDocumentService().didSave(paramsSave);
  }

  public CompletableFuture<List<? extends TextEdit>> textDocumentFormatting(URI uri) {
    var paramsFormatting = new DocumentFormattingParams();
    var identifier = new TextDocumentIdentifier();
    identifier.setUri(uri.toString());
    paramsFormatting.setTextDocument(identifier);
    var options = new FormattingOptions(4, false);
    paramsFormatting.setOptions(options);
    return server.getTextDocumentService().formatting(paramsFormatting);
  }

  public CompletableFuture<Object> shutdown() {
    return server.shutdown();
  }

  public void exit() {
    server.exit();
  }


}
