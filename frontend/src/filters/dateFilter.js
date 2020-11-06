import moment from "moment";

export default function dateFilter(date, format = 'YYYY-MM-DD') {
    return moment(date).format(format);
}