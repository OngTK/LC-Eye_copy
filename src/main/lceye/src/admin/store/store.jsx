import { configureStore } from "@reduxjs/toolkit";
import adminReducer from './adminSlice.jsx';
import projectReducer from './projectSlice.jsx';


const store = configureStore({
    reducer : {
        admin : adminReducer,
        project: projectReducer,
    }
});

export default store;