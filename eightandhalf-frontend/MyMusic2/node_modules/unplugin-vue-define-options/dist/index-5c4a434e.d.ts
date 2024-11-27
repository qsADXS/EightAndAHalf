declare function transformDefineOptions(code: string, id: string): {
    code: string;
    map: any;
} | undefined;

export { transformDefineOptions as t };
