import { createSlice } from "@reduxjs/toolkit";

const initialState = {
    selectedProject: null,
    projectListVersion: 0,
    basicInfoStatus: {
        isInitialized: false, // 기본정보가 로드/저장된 상태인지
        isSaved: false,       // 마지막 변경사항이 서버에 저장된 상태인지
        isDirty: false,       // 저장 이후 변경된 값이 있는지
    },
};

const projectSlice = createSlice({
    name: "project",
    initialState,
    reducers: {
        setSelectedProject: (state, action) => {
            state.selectedProject = action.payload;
            const hasProject = !!action.payload;
            state.basicInfoStatus = {
                isInitialized: hasProject,
                isSaved: hasProject,
                isDirty: false,
            };
        },
        clearSelectedProject: (state) => {
            state.selectedProject = null;
            state.basicInfoStatus = {
                isInitialized: false,
                isSaved: false,
                isDirty: false,
            };
        },
        incrementProjectListVersion: (state) => {
            state.projectListVersion += 1;
        },
        setBasicInfoDirty: (state, action) => {
            const dirty = action.payload;
            state.basicInfoStatus.isDirty = dirty;
            if (dirty) {
                state.basicInfoStatus.isSaved = false;
            }
        },
        markBasicInfoSaved: (state) => {
            state.basicInfoStatus.isInitialized = !!state.selectedProject;
            state.basicInfoStatus.isSaved = true;
            state.basicInfoStatus.isDirty = false;
        },
    },
});

export default projectSlice.reducer;
export const {
    setSelectedProject,
    clearSelectedProject,
    incrementProjectListVersion,
    setBasicInfoDirty,
    markBasicInfoSaved,
} = projectSlice.actions;

