import moment from "moment";
import numeral from 'numeral';

export function date(date, format = 'YYYY-MM-DD') {
    return moment(date).format(format);
}

export function fromNow(date) {
    return moment(date).fromNow();
}

export function number(value, format) {
    return numeral(value).format(format);
}

export default {
    date,
    fromNow,
    number
}
