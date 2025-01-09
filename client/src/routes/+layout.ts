import type { LayoutLoad } from './$types';
import type { PageFetch } from '$lib/data';
import { PUBLIC_BACKEND_URL } from '$env/static/public';

export interface User {
	name: string;
	email?: string;
	role: {
		challenger?: boolean;
		admin?: boolean;
		moderator?: boolean;
		author?: boolean;
	};
	createdAt?: Date;
	deletedAt?: Date;
}

export const load: LayoutLoad = async ({ fetch, url }) => {
	return {
		user: await fetchUser(fetch)
	};
};

async function fetchUser(fetch: PageFetch): Promise<User | null> {
	let res = await fetch(`${PUBLIC_BACKEND_URL}/profile`);

	// User isn't logged in
	if (res.status == 401) {
		return null;
	}

	if (res.status != 200) {
		console.error(`Unexpected status code when fetching profile: ${res.status}`);
		return null;
	}

	const json = await res.json();

	if (json?.authentication == null) {
		console.error(`User missing authentication field in response: ${json}`);
		return null;
	}

	return {
		name: json.authentication,
		email: json?.primaryContact,
		role: {
			challenger: json?.isChallenger,
			admin: json?.isAdmin,
			moderator: json?.isModerator,
			author: json?.isAuthor
		},
		createdAt: json.createdAt ? new Date(json.createdAt) : undefined,
		deletedAt: json.deletedAt ? new Date(json.deletedAt) : undefined
	};
}
