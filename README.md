# Polypack Host
Automatically build, host and require all your installed polymer mods resources!

## Config `(config/polypack_host.json)`
* `external_ip`: The IP address to host the resource pack on. Leave empty to use the IP server's properties file.
    * Default: ``
* `host_port`: The port to host the resource pack on. Make sure this port is also forwarded, so it can be accessed externally.
    * Default: `24464`
* `thread_count`: The number of threads the HTTP server is allowed to use. If the server cannot keep up with the requests, this may need to be increased.
    * Default: `1`
* `randomise_url`: If this is set to true, it will make the client download a new version of the resource pack each time. If you have issues with players joining and being told there was a resource pack error, set this to `true`.
    * Default: `false`

## Recommended Server Properties `(server.properties)`
* Set `require-resource-pack` to `true` so the client is mostly forced to use the resource pack.
* Set `resource-pack-prompt` to something like `"Required Mod Resources""` so the player knows why the pack is required.

## FAQ
* I get a `No external IP address is defined in the configuration, this may cause errors outside of the local network.` warning everytime I start the server, what's the problem?
  * If you do not have an external IP defined, the mod cannot tell the client's where to find pack. 
  * Set the external IP in the configuration or server.properties file to fix.
* Some/All clients fail to download the resource pack.
  * The URL that is sent to the client is logged on server start, check this URL to make sure it looks right. Try opening the URL in your browser, it should also download the resource pack file.
  * Make sure the specified port is forwarded and accessible externally.
