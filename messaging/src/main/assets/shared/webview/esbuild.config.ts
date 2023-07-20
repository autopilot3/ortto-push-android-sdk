import esbuild from 'esbuild';
import copyPlugin from 'esbuild-plugin-copy';
import appRoot from 'app-root-path';

esbuild.build({
    entryPoints: ["./src/app.ts"],
    outdir: appRoot.resolve("www"),
    bundle: true,
    minify: process.env.NODE_ENV === 'production',
    sourcemap: false,
    plugins: [
        copyPlugin({
            assets: [{
                from: "./src/index.html",
                to: "index.html",
            }],
        }),
    ],
    legalComments: 'none',
});
