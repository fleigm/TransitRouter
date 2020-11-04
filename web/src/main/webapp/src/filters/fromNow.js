import moment from "moment";

export default function fromNow(date) {
    return moment(date).fromNow();
}