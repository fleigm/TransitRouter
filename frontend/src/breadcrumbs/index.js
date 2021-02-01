import {reactive} from "@vue/composition-api";

const state = reactive({
    items: [],
});

function append(item) {
    state.items.push(item);
}

function pop() {
    state.items.pop();
    console.log('pop');
}

function clear() {
    state.items.slice(0);
}

function set(items) {
    if (!Array.isArray(items)) {
        items = [items];
    }

    clear()
    state.items.push(...items);
    console.log(state);
}

export default {
    state,
    items: state.items,
    append,
    pop,
    clear,
    set,
}

