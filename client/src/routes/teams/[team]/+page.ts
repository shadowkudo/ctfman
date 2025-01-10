import type { PageLoad } from './$types';
import type { PageFetch } from '$lib/data';
import { PUBLIC_BACKEND_URL } from '$env/static/public';
import { error, redirect } from '@sveltejs/kit';

interface Team {
	name: string;
	description?: string;
	country?: string;
	captain?: string;
	createdAt?: Date;
	deletedAt?: Date;
}

export const load: PageLoad = async ({ fetch, params }) => {
	let team: Team | null = await fetchTeam(fetch, params.team);

	if (!team) {
		error(404, 'Not found');
	}

	return {
		team
	};
};

async function fetchTeam(fetch: PageFetch, name: string): Promise<Team | null> {
	let res = await fetch(`${PUBLIC_BACKEND_URL}/teams/${name}`, { credentials: 'include' });

	if (res.status != 200) {
		console.error(`teams/+page.tx@fetchUser: unexpected status: ${res.status}`);
		return null;
	}

	const json = await res.json();

	return {
		name: json.authentication,
		description: json.description,
		country: json.country,
		captain: json.captain,
		createdAt: json.createdAt ? new Date(json.createdAt) : undefined,
		deletedAt: json.deletedAt ? new Date(json.deletedAt) : undefined
	};
}
