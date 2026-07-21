import tailwindcss from '@tailwindcss/postcss';
import autoprefixer from 'autoprefixer';

export default {
    plugins: [
        tailwindcss({
            // Wir zwingen das Plugin, die HTML-Dateien direkt auf Festplatte zu suchen
            content: [
                './src/app/**/*.html',
                './src/app/**/*.ts',
                './src/index.html'
            ]
        }),
        autoprefixer(),
    ],
};