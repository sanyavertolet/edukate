export interface CreateBundleRequest {
    name: string;
    description: string;
    isPublic: boolean;
    problemIds: string[];
}
