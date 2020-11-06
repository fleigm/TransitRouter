export function copyObject(obj) {
    return JSON.parse(JSON.stringify(obj));
}

export const Helper = {
    copyObject,
}