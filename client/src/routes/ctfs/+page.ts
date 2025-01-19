import type { PageLoad } from './$types';
import { fetchAllCtfs } from '$lib/data';

export const load: PageLoad = async ({ fetch }) => {
	return {
		ctfs: await fetchAllCtfs(fetch)
	};
};
