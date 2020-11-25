export function copyObject(obj) {
    return JSON.parse(JSON.stringify(obj));
}

export function objectToFormData(obj) {
    const formData = new FormData();
    for (const [key, value] of Object.entries(obj)) {
        formData.append(key, value);
    }
    return formData;
}

export const Helper = {
    copyObject,
}