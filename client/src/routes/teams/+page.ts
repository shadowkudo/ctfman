import type { PageLoad } from './$types';
import type { PageFetch } from '$lib/data';
import { PUBLIC_BACKEND_URL } from '$env/static/public';
import { redirect } from '@sveltejs/kit';

interface Team {
	name: string;
	description?: string;
	country?: string;
	captain?: string;
	createdAt?: Date;
	deletedAt?: Date;
}

export const load: PageLoad = async ({ fetch, url }) => {
	return {
		teams: await fetchUser(fetch)
	};
};

async function fetchUser(fetch: PageFetch): Promise<Team[]> {
	let res = await fetch(`${PUBLIC_BACKEND_URL}/teams`, { credentials: 'include' });

	if (res.status == 401) {
		redirect(307, '/login');
	}

	if (res.status != 200) {
		console.error(`teams/+page.tx@fetchUser: unexpected status: ${res.status}`);
		return [];
	}

	const json = await res.json();

	if (!Array.isArray(json)) {
		console.error(`teams/+page.tx@fetchUser: unexpected json body: ${json}`);
		return [];
	}

	return json.map((it) => ({
		name: it.authentication,
		description: it.description,
		country: it.country,
		captain: it.captain,
		createdAt: it.createdAt ? new Date(it.createdAt) : undefined,
		deletedAt: it.deletedAt ? new Date(it.deletedAt) : undefined
	}));
}
