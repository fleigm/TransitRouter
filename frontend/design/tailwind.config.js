const defaultTheme = require('tailwindcss/defaultTheme')

module.exports = {
    purge: {
        content: [
            './index.html',
            './src/**/*.html',
            './src/**/*.vue',
            './src/**/*.jsx',
        ],
    },
    theme: {
        extend: {
            height: {
                '96': '20rem',
                '128': '24rem',
            },
            minHeight: {
                '128': '24rem',
            }
        },
        colors: {
            ...defaultTheme.colors,

            'primary': '#22292f',
            'secondary': '#606f7b',
            'tertiary': '#8795a1',

            'primary-inverse': '#ffffff',
            'secondary-inverse': '#f1f5f8',
            'tertiary-inverse': '#dae1e7',
        },
        container: {
            center: true,
            padding: '2rem',
            screens: {
                sm: "100%",
                md: "100%",
                lg: "1024px",
                xl: "1280px"
            }
        },
        fontFamily: {
            sans: [
                '"Inter"',
                'system-ui',
                '-apple-system',
                'BlinkMacSystemFont',
                '"Segoe UI"',
                '"Helvetica Neue"',
                'Arial',
                '"Noto Sans"',
                'sans-serif',
                '"Apple Color Emoji"',
                '"Segoe UI Emoji"',
                '"Segoe UI Symbol"',
                '"Noto Color Emoji"',
            ],
        },
        fontSize: defaultTheme.fontSize,
    },
    variants: {
        extend: {
            backgroundColor: ['odd'],
            display: ['group-hover'],
        }
    },
    plugins: [],
}
