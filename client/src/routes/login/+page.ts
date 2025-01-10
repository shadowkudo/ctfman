import { redirect } from '@sveltejs/kit';
import type { PageLoad } from './$types';

export const load: PageLoad = async ({ fetch, url, parent }) => {
	const { user } = await parent();

	// Automatically redirect the user to login if not logged in
	if (user) {
		redirect(307, '/');
	}

	return {};
};
