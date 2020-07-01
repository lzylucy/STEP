// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import com.google.sps.Event;
import com.google.sps.TimeRange;
import com.google.sps.MeetingRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, 
                                      MeetingRequest request) {
    ArrayList<TimeRange> unavailable = new ArrayList<>();
    Collection<TimeRange> result = new ArrayList<>();

    // Get unavailable time for all required attendees in meeting request
    for (String attendee: request.getAttendees()) {
      ArrayList<TimeRange> meetingTimes = getMeetingTimes(events, attendee);
      unavailable = merge(unavailable, meetingTimes);
    }

    // Get time ranges that are longer than the requested meeting duration
    // for required attendees
    for (TimeRange t: getAvailability(unavailable)) {
      if (t.duration() >= request.getDuration()) {
        result.add(t);
      }
    }

    // Consider optional attendees
    ArrayList<TimeRange> unavailableWithOptional = new ArrayList<>();
    Collection<TimeRange> resultWithOptional = new ArrayList<>();
    unavailableWithOptional.addAll(unavailable);

    if (!request.getOptionalAttendees().isEmpty()) {
      // Merge unavailable time for all optional attendees in meeting request
      for (String attendee: request.getOptionalAttendees()) {
        ArrayList<TimeRange> meetingTimes = getMeetingTimes(events, attendee);
        unavailableWithOptional = merge(unavailableWithOptional, meetingTimes);
      }

      // Get time ranges that are longer than the requested meeting duration
      // for all attendees
      for (TimeRange t: getAvailability(unavailableWithOptional)) {
        if (t.duration() >= request.getDuration()) {
          resultWithOptional.add(t);
        }
      }
    } else {
      // If there are no optional attendees, result does not change
      resultWithOptional.addAll(result);
    }

    // If there are no mandatory attendees, treat optional as mandatory
    if (request.getAttendees().isEmpty()) {
      return resultWithOptional;
    }

    return ((resultWithOptional.isEmpty()) ? result : resultWithOptional);
  }

  private ArrayList<TimeRange> merge(ArrayList<TimeRange> result, 
                                      ArrayList<TimeRange> other) {
    if (result.isEmpty()) {
      return other;
    }
    if (other.isEmpty()) {
      return result;
    }

    result.addAll(other);
    Collections.sort(result, TimeRange.ORDER_BY_START);

    // Merge overlapping ranges
    for (int i = 0; i < result.size()-1; i++) {
      TimeRange current = result.get(i);
      TimeRange next = result.get(i+1);

      if (current.overlaps(next)) {
        // Case 1:  |_______|
        //            |__|
        if (current.contains(next)) {
          result.remove(next);
          i--;
        }
        // Case 2:  |______|
        //               |______|
        else {
          result.set(i, TimeRange.fromStartEnd(current.start(), 
                                                next.end(), false));
          result.remove(next);
          i--;
        }
      }
    }
    
    return result;
  }

  /**
   * Gets an attendee's unavailable time ranges
   * Assume required meetings for one person do not overlap
   */
  private ArrayList<TimeRange> getMeetingTimes(Collection<Event> events, 
                                                String attendee) {
    ArrayList<TimeRange> meetingTimes = new ArrayList<>();
    for (Event e: events) {
      if (e.getAttendees().contains(attendee)) {
        meetingTimes.add(e.getWhen());
      }
    }

    return meetingTimes;
  }

  /**
   * Returns attendee's available time ranges given unavailable time ranges
   */
  private Collection<TimeRange> getAvailability(Collection<TimeRange> unavailable) {
    Collection<TimeRange> availability = new ArrayList<>();
    int point = TimeRange.START_OF_DAY;
    for (TimeRange t: unavailable) {
      availability.add(TimeRange.fromStartEnd(point, t.start(), false));
      point = t.end();
    }
    availability.add(TimeRange.fromStartEnd(point, TimeRange.END_OF_DAY, true));

    return availability;
  }
}