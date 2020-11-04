import numeral from 'numeral';

export default function number(value, format) {
    return numeral(value).format(format);
}